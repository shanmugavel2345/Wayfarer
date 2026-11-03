package com.shank.booking_service.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record BookingRequest(
        @NotNull Long busId,
        @NotBlank String passengerName,
        @NotBlank @Email String passengerEmail,
        @NotBlank String passengerPhone,
        @JsonProperty("jouneyDate") @NotNull @FutureOrPresent LocalDate journeyDate,
        @NotNull @Min(1) Integer numberOfSeats) {
}