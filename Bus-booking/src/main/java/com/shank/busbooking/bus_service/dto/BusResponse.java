package com.shank.busbooking.dto;

// [AI GENERATED] Removed unused import (response DTOs don't need validation annotations):
// import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusResponse {

    private Long id;
    private String busName;
    private String busNumber;
    private String source;
    private String destination;
    private Integer totalSeates;
    private BigDecimal fare;
}
