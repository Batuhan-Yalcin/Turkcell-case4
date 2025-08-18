package com.turkcellcase4.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioComparisonDTO {
    private String scenarioName;
    private BigDecimal totalCost;
    private BigDecimal saving;
    private String description;
    private SimulationScenarioDTO scenario;
    private BigDecimal newTotal;
    private BigDecimal savings;
}
