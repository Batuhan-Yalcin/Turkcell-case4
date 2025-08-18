package com.turkcellcase4.simulation.service;

import com.turkcellcase4.simulation.dto.SimulationRequestDTO;
import com.turkcellcase4.simulation.dto.SimulationResponseDTO;

public interface SimulationService {
    
    SimulationResponseDTO simulateScenario(SimulationRequestDTO request);
    
    SimulationResponseDTO getScenarios(Long userId);
    
    SimulationResponseDTO compareScenarios(SimulationRequestDTO request);
    
    SimulationResponseDTO getWhatIfAnalysis(Long userId, String period);
}
