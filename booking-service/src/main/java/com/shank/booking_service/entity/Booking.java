package com.shank.booking_service.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long busId;
    @Column(nullable = false)
    private String passengerName;
    @Column(nullable = false)
    private String passengerEmail;
    @Column(nullable = false)
    private String passengerPhone;
    @Column(nullable = false)
    private LocalDate journeyDate;
    @Column(nullable = false)
    private Integer numberOfSeats;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus bookingStatus;

    public Booking(Long busId, String passengerName, String passengerEmail,
                   String passengerPhone, LocalDate journeyDate, Integer numberOfSeats,
                   BigDecimal totalAmount) {
        this.busId = busId;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.passengerPhone = passengerPhone;
        this.journeyDate = journeyDate;
        this.numberOfSeats = numberOfSeats;
        this.totalAmount = totalAmount;
        this.bookingStatus = BookingStatus.PENDING;
    }

    public void cancel() {
        this.bookingStatus = BookingStatus.CANCELLED;
    }
}