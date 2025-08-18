package com.turkcellcase4.billing.dto;

import com.turkcellcase4.common.enums.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBreakdownDTO {
    private ItemCategory category;
    private BigDecimal total;
    private List<BillItemDTO> lines;
    private String explanation;
}
