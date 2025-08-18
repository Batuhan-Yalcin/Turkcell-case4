package com.turkcellcase4.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationScenarioDTO {
    private Long planId;
    private List<Long> addons;
    private Boolean disableVas;
    private Boolean blockPremiumSms;
    private String description;
}
