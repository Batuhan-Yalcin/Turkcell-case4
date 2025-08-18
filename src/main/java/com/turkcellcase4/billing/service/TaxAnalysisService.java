package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.TaxBreakdownDTO;

public interface TaxAnalysisService {
    
    /**
     * Fatura için detaylı vergi ayrıştırması yapar
     */
    TaxBreakdownDTO analyzeTaxBreakdown(Long billId);
    
    /**
     * Kullanıcının vergi trendini analiz eder
     */
    TaxBreakdownDTO analyzeUserTaxTrend(Long userId, int months);
    
    /**
     * Vergi optimizasyonu önerileri sunar
     */
    String getTaxOptimizationSuggestions(Long billId);
    
    /**
     * Vergi karşılaştırması yapar
     */
    TaxBreakdownDTO compareTaxRates(Long billId1, Long billId2);
}
