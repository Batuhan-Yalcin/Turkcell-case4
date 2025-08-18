package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.AnomalyDTO;

public interface LLMExplanationService {
    
    /**
     * Anomali için AI destekli açıklama üretir
     */
    String generateAnomalyExplanation(AnomalyDTO anomaly, String userContext);
    
    /**
     * Kohort kıyası için AI destekli analiz üretir
     */
    String generateCohortAnalysis(Long userId, String period, Double userAverage, Double cohortAverage);
    
    /**
     * Vergi ayrıştırması için AI destekli analiz üretir
     */
    String generateTaxBreakdownAnalysis(Long billId, Double totalTax, Double effectiveTaxRate);
    
    /**
     * Autofix önerisi için AI destekli senaryo üretir
     */
    String generateAutofixRecommendation(Long userId, String period, Double currentCost, Double potentialSavings);
    
    /**
     * Genel fatura analizi için AI destekli özet üretir
     */
    String generateBillAnalysisSummary(Long billId, String period, Double totalAmount, String mainCategories);
}
