package com.turkcellcase4.billing.controller;

import com.turkcellcase4.billing.dto.AnomalyRequestDTO;
import com.turkcellcase4.billing.dto.AnomalyResponseDTO;
import com.turkcellcase4.billing.service.AnomalyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/anomalies")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnomalyController {

    private final AnomalyService anomalyService;

    @PostMapping
    public ResponseEntity<AnomalyResponseDTO> detectAnomalies(@Valid @RequestBody AnomalyRequestDTO request) {
        log.info("POST /anomalies - Detecting anomalies for user: {} and period: {}", request.getUserId(), request.getPeriod());
        AnomalyResponseDTO response = anomalyService.detectAnomalies(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<AnomalyResponseDTO> getAnomalies(@RequestParam Long userId, @RequestParam String period) {
        log.info("GET /anomalies - Getting anomalies for user: {} and period: {}", userId, period);
        AnomalyRequestDTO request = new AnomalyRequestDTO();
        request.setUserId(userId);
        request.setPeriod(period);
        AnomalyResponseDTO response = anomalyService.detectAnomalies(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<AnomalyResponseDTO> getAnomalyHistory(@PathVariable Long userId) {
        log.info("GET /anomalies/{}/history - Getting anomaly history", userId);
        AnomalyResponseDTO response = anomalyService.getAnomalyHistory(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/summary")
    public ResponseEntity<AnomalyResponseDTO> getAnomalySummary(@PathVariable Long userId) {
        log.info("GET /anomalies/{}/summary - Getting anomaly summary", userId);
        AnomalyResponseDTO response = anomalyService.getAnomalySummary(userId);
        return ResponseEntity.ok(response);
    }
}
