package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutofixRecommendationDTO {
    
    private String autofixId;
    private Long userId;
    private String period;
    private String scenarioName;
    
    // Senaryo detayları
    private String description;
    private String category; // "PLAN_CHANGE", "ADDON_ADD", "VAS_CANCEL", "PREMIUM_SMS_BLOCK"
    private Map<String, Object> scenarioDetails;
    
    // Maliyet analizi
    private BigDecimal currentCost;
    private BigDecimal newCost;
    private BigDecimal potentialSavings;
    private BigDecimal savingsPercentage;
    
    // Öncelik ve risk
    private Integer priority; // 1-5 (1=en yüksek)
    private String riskLevel; // "LOW", "MEDIUM", "HIGH"
    private String implementationDifficulty; // "EASY", "MEDIUM", "HARD"
    
    // AI açıklaması
    private String aiExplanation;
    private String aiReasoning;
    
    // Aksiyonlar
    private List<String> requiredActions;
    private String implementationSteps;
    
    // Geçerlilik
    private boolean isValid;
    private String validationMessage;
    
    // Uygulama durumu
    private String status; // "PENDING", "APPLIED", "FAILED"
    private String appliedAt;
    private String resultMessage;
}
