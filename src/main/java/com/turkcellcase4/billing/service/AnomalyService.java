package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.AnomalyRequestDTO;
import com.turkcellcase4.billing.dto.AnomalyResponseDTO;

public interface AnomalyService {
    
    AnomalyResponseDTO detectAnomalies(AnomalyRequestDTO request);
    
    AnomalyResponseDTO getAnomalyHistory(Long userId);
    
    AnomalyResponseDTO getAnomalySummary(Long userId);
}
