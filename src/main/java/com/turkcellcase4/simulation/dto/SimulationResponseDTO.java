package com.turkcellcase4.simulation.dto;

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
public class SimulationResponseDTO {
    private BigDecimal currentTotal;
    private BigDecimal newTotal;
    private BigDecimal saving;
    private String details;
    private SimulationScenarioDTO scenario;
    private List<SimulationScenarioDTO> scenarios;
    private List<ScenarioComparisonDTO> comparisons;
    private List<ScenarioComparisonDTO> topScenarios;
    private List<String> recommendations;
    private String summary;
}
