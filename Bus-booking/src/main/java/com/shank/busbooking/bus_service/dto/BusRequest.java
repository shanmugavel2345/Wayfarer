package com.shank.busbooking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
// [AI GENERATED] Removed unused import (nothing in this class uses it):
// import org.apache.logging.log4j.message.Message;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusRequest {

    @NotBlank(message = "Bus name is req")
    private String busName;

    @NotBlank(message = "Bus number is req")
    private String busNumber;

    @NotBlank(message = "Bus source req")
    private String source;

    @NotBlank(message = "Bus dust req")
    private String destination;

    @NotNull(message = "Total seats is req")
    @Min(value = 1, message = "Total seates must be at least 1")
    private Integer totalSeates;

    @NotNull(message = "Fare is req")
    @DecimalMin(value = "0.01", message = "Fare must greater than zero")
    private BigDecimal fare;
}
