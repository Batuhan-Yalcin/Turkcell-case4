package com.turkcellcase4.billing.dto;

import com.turkcellcase4.common.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemDTO {
    private Long itemId;
    private ItemCategory category;
    private String subtype;
    private String description;
    private BigDecimal amount;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal taxRate;
}
