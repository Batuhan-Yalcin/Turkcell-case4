package com.turkcellcase4.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationRequestDTO {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Period is required")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "Period must be in YYYY-MM format")
    private String period;
    
    private SimulationScenarioDTO scenario;
}
