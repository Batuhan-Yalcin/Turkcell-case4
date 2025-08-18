package com.turkcellcase4.simulation.controller;

import com.turkcellcase4.simulation.dto.SimulationRequestDTO;
import com.turkcellcase4.simulation.dto.SimulationResponseDTO;
import com.turkcellcase4.simulation.service.SimulationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whatif")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping
    public ResponseEntity<SimulationResponseDTO> simulate(@Valid @RequestBody SimulationRequestDTO request) {
        log.info("POST /whatif - Simulate scenario");
        return ResponseEntity.ok(simulationService.simulateScenario(request));
    }

    @GetMapping("/{userId}/scenarios")
    public ResponseEntity<SimulationResponseDTO> getScenarios(@PathVariable Long userId) {
        log.info("GET /whatif/{}/scenarios - Get scenarios", userId);
        return ResponseEntity.ok(simulationService.getScenarios(userId));
    }

    @PostMapping("/compare")
    public ResponseEntity<SimulationResponseDTO> compare(@Valid @RequestBody SimulationRequestDTO request) {
        log.info("POST /whatif/compare - Compare scenarios");
        return ResponseEntity.ok(simulationService.compareScenarios(request));
    }

    @GetMapping("/{userId}/analysis")
    public ResponseEntity<SimulationResponseDTO> getWhatIfAnalysis(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /whatif/{}/analysis - Get what-if analysis for period: {}", userId, period);
        return ResponseEntity.ok(simulationService.getWhatIfAnalysis(userId, period));
    }
}
