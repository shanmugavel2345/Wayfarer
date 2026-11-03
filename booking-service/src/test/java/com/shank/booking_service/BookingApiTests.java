package com.shank.booking_service;

import java.time.LocalDate;
import com.shank.booking_service.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingApiTests {
    @Autowired private MockMvc mvc;
    @Autowired private BookingRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private String request() {
        return """
                {"busId":12,"passengerName":"Test Passenger",
                 "passengerEmail":"test@example.com","passengerPhone":"1234567890",
                 "jouneyDate":"%s","numberOfSeats":3,
                 "totalAmount":1,"bookingStatus":"CONFIRMED"}
                """.formatted(LocalDate.now());
    }

    private Long createBooking() throws Exception {
        mvc.perform(post("/api/booking/createBooking")
                        .contentType(MediaType.APPLICATION_JSON).content(request()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.busId").value(12))
                .andExpect(jsonPath("$.passengerName").value("Test Passenger"))
                .andExpect(jsonPath("$.passengerEmail").value("test@example.com"))
                .andExpect(jsonPath("$.passengerPhone").value("1234567890"))
                .andExpect(jsonPath("$.journeyDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.jouneyDate").doesNotExist())
                .andExpect(jsonPath("$.numberOfSeats").value(3))
                .andExpect(jsonPath("$.totalAmount").value(1500.00))
                .andExpect(jsonPath("$.bookingStatus").value("PENDING"));
        return repository.findAll().get(0).getId();
    }

    @Test
    void createsAndPersistsServerCalculatedBooking() throws Exception {
        Long id = createBooking();
        var saved = repository.findById(id).orElseThrow();
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("1500.00");
        assertThat(saved.getBookingStatus().name()).isEqualTo("PENDING");
    }

    @Test
    void listsEmptyAndPopulatedBookingsAndLooksUpById() throws Exception {
        mvc.perform(get("/api/booking/getAllBookings"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        Long id = createBooking();
        mvc.perform(get("/api/booking/getAllBookings"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/booking/{id}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void cancellationIsPersistentAndIdempotentWithoutDeleting() throws Exception {
        Long id = createBooking();
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(put("/api/booking/{id}/cancel", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"))
                    .andExpect(jsonPath("$.totalAmount").value(1500));
        }
        mvc.perform(get("/api/booking/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("CANCELLED"));
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void missingLookupAndCancellationReturn404() throws Exception {
        mvc.perform(get("/api/booking/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with id: 999999"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.details").doesNotExist());
        mvc.perform(put("/api/booking/999999/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with id: 999999"));
    }

    @Test
    void missingRequiredFieldsHaveValidationDetails() throws Exception {
        mvc.perform(post("/api/booking/createBooking")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.busId").exists())
                .andExpect(jsonPath("$.details.passengerName").exists())
                .andExpect(jsonPath("$.details.passengerEmail").exists())
                .andExpect(jsonPath("$.details.passengerPhone").exists())
                .andExpect(jsonPath("$.details.jouneyDate").exists())
                .andExpect(jsonPath("$.details.numberOfSeats").exists());
        assertThat(repository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "email", "phone", "date", "seats", "blankEmail"})
    void rejectsInvalidFields(String field) throws Exception {
        String body = switch (field) {
            case "name" -> request().replace("Test Passenger", " ");
            case "email" -> request().replace("test@example.com", "invalid");
            case "blankEmail" -> request().replace("test@example.com", " ");
            case "phone" -> request().replace("1234567890", " ");
            case "date" -> request().replace(LocalDate.now().toString(), LocalDate.now().minusDays(1).toString());
            default -> request().replace("\"numberOfSeats\":3", "\"numberOfSeats\":0");
        };
        mvc.perform(post("/api/booking/createBooking").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details").isMap());
        assertThat(repository.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "", "null", "invalidDate", "invalidSeats"})
    void malformedBodiesReturn400(String input) throws Exception {
        String body = switch (input) {
            case "invalidDate" -> request().replace(LocalDate.now().toString(), "2026-02-30");
            case "invalidSeats" -> request().replace("\"numberOfSeats\":3", "\"numberOfSeats\":\"abc\"");
            default -> input;
        };
        mvc.perform(post("/api/booking/createBooking").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void malformedPathVariablesReturn400() throws Exception {
        mvc.perform(get("/api/booking/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request"));
        mvc.perform(put("/api/booking/not-a-number/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request"));
    }

            @Test
            void configuredDecimalPriceIsUsedExactly() {
            var service = new com.shank.booking_service.service.BookingService(
                repository, new java.math.BigDecimal("123.45"));
            var result = service.create(new com.shank.booking_service.dto.BookingRequest(
                12L, "Test", "test@example.com", "1234567890", LocalDate.now(), 3));
            assertThat(result.totalAmount()).isEqualByComparingTo("370.35");
            }

            @Test
            void openApiAndSwaggerAreAvailable() throws Exception {
            mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/booking/createBooking'].post").exists())
                .andExpect(jsonPath("$.components.schemas.BookingRequest.properties.jouneyDate").exists())
                .andExpect(jsonPath("$.components.schemas.BookingResponse.properties.journeyDate").exists());
            mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
            }

            @Test
            void unexpectedErrorBodyDoesNotExposeExceptionDetails() {
            var handler = new com.shank.booking_service.exception.GlobalExceptionHandler();
            var response = handler.unexpected(new IllegalStateException("internal failure"));
            assertThat(response.getStatusCode().value()).isEqualTo(500);
            assertThat(response.getBody()).containsOnlyKeys("timestamp", "status", "error", "message")
                .containsEntry("message", "Unexpected error occurred")
                .containsEntry("status", 500)
                .containsEntry("error", "Internal Server Error");
            }
}