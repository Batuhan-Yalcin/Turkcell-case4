package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplainResponseDTO {
    private BillSummaryDTO summary;
    private List<CategoryBreakdownDTO> breakdown;
    private String naturalLanguageSummary;
}
