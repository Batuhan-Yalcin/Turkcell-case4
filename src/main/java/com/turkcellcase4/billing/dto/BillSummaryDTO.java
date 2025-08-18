package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillSummaryDTO {
    private BigDecimal totalAmount;
    private BigDecimal taxes;
    private BigDecimal usageBasedCharges;
    private BigDecimal oneTimeCharges;
    private String savingsHint;
}
