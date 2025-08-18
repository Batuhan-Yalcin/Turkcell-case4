package com.turkcellcase4.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumSMSDTO {
    private String shortcode;
    private String provider;
    private BigDecimal unitPrice;
}
