package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CohortAnalysisDTO {
    
    private Long userId;
    private String period;
    private String userType;
    
    // Kullanıcı verileri
    private BigDecimal userAverage;
    private BigDecimal userTotal;
    private Integer userBillCount;
    
    // Kohort verileri
    private BigDecimal cohortAverage;
    private BigDecimal cohortTotal;
    private Integer cohortUserCount;
    
    // Karşılaştırma
    private BigDecimal difference;
    private BigDecimal percentageDifference;
    private String performanceRating; // "ABOVE_AVERAGE", "AVERAGE", "BELOW_AVERAGE"
    
    // AI açıklaması
    private String aiExplanation;
    
    // Benzer kullanıcılar
    private List<Long> similarUserIds;
    private String similarityReason;
    
    // Öneriler
    private String recommendation;
    private BigDecimal potentialSavings;
}
