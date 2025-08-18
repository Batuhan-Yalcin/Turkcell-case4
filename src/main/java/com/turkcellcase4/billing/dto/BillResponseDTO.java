package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseDTO {
    private Long billId;
    private Long userId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate issueDate;
    private BigDecimal totalAmount;
    private String currency;
    private List<BillItemDTO> items;
}
