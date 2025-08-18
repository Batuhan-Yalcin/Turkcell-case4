package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.AutofixRecommendationDTO;

import java.util.List;

public interface AutofixService {
    
    /**
     * Kullanıcı için en iyi autofix önerisini üretir
     */
    AutofixRecommendationDTO generateBestAutofix(Long userId, String period);
    
    /**
     * Tüm olası autofix senaryolarını listeler
     */
    List<AutofixRecommendationDTO> getAllAutofixScenarios(Long userId, String period);
    
    /**
     * Belirli bir autofix senaryosunu uygular
     */
    String applyAutofix(Long userId, String autofixId);
    
    /**
     * Autofix önerilerini öncelik sırasına göre sıralar
     */
    List<AutofixRecommendationDTO> getPrioritizedAutofixes(Long userId, String period);
    
    /**
     * Autofix önerilerinin geçerliliğini kontrol eder
     */
    boolean validateAutofix(Long userId, String autofixId);
}
