package com.turkcellcase4.billing.dto;

import com.turkcellcase4.common.enums.AnomalyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDTO {
    private AnomalyType type;
    private String category;
    private String subtype;
    private BigDecimal delta;
    private BigDecimal percentageChange;
    private BigDecimal zScore;
    private String reason;
    private String suggestedAction;
    private String severity;
}
