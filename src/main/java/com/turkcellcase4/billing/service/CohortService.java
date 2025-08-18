package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.CohortAnalysisDTO;

public interface CohortService {
    
    /**
     * Kullanıcının benzer kullanıcılarla karşılaştırmasını yapar
     */
    CohortAnalysisDTO analyzeUserCohort(Long userId, String period);
    
    /**
     * Belirli kullanıcı tipindeki kullanıcıların ortalamasını hesaplar
     */
    Double getCohortAverage(String userType, String period);
    
    /**
     * Kullanıcının kohort ortalamasına göre performansını değerlendirir
     */
    String evaluateUserPerformance(Long userId, String period);
    
    /**
     * Benzer kullanım profiline sahip kullanıcıları bulur
     */
    CohortAnalysisDTO findSimilarUsers(Long userId, String period);
}
