package com.turkcellcase4.billing.dto;

import com.turkcellcase4.common.enums.AnomalyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyResponseDTO {
    private List<AnomalyDTO> anomalies;
    private int totalAnomalies;
    private String period;
    private Long userId;
    private Map<AnomalyType, Long> anomalySummary;
}
