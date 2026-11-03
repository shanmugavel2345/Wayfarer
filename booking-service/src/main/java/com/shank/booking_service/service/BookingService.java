package com.shank.booking_service.service;

import java.math.BigDecimal;
import java.util.List;
import com.shank.booking_service.dto.BookingRequest;
import com.shank.booking_service.dto.BookingResponse;
import com.shank.booking_service.entity.Booking;
import com.shank.booking_service.exception.BookingNotFoundException;
import com.shank.booking_service.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private final BookingRepository repository;
    private final BigDecimal pricePerSeat;

    public BookingService(BookingRepository repository,
                          @Value("${booking.price-per-seat:500.00}") BigDecimal pricePerSeat) {
        this.repository = repository;
        this.pricePerSeat = pricePerSeat;
    }

    public BookingResponse create(BookingRequest request) {
        BigDecimal total = pricePerSeat.multiply(BigDecimal.valueOf(request.numberOfSeats()));
        Booking booking = repository.save(new Booking(request.busId(), request.passengerName(),
                request.passengerEmail(), request.passengerPhone(), request.journeyDate(),
                request.numberOfSeats(), total));
        log.info("Created booking id={}", booking.getId());
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findAll() {
        return repository.findAll().stream().map(BookingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse findById(Long id) {
        return BookingResponse.from(requireBooking(id));
    }

    public BookingResponse cancel(Long id) {
        Booking booking = requireBooking(id);
        booking.cancel();
        Booking saved = repository.save(booking);
        log.info("Cancelled booking id={}", id);
        return BookingResponse.from(saved);
    }

    private Booking requireBooking(Long id) {
        return repository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }
}