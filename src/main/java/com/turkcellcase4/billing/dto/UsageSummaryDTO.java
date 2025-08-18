package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageSummaryDTO {
    
    private Long userId;
    private String period; // YYYY-MM formatında
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Toplam kullanım
    private Double totalDataGB;
    private Integer totalVoiceMinutes;
    private Integer totalSMSCount;
    private Double totalRoamingGB;
    
    // Ortalama günlük kullanım
    private Double averageDailyDataGB;
    private Integer averageDailyVoiceMinutes;
    private Integer averageDailySMSCount;
    private Double averageDailyRoamingGB;
    
    // En yüksek kullanım günleri
    private LocalDate peakDataDate;
    private LocalDate peakVoiceDate;
    private LocalDate peakSMSDate;
    
    // Kullanım trendi
    private String dataTrend; // "INCREASING", "DECREASING", "STABLE"
    private String voiceTrend;
    private String smsTrend;
    
    // Tasarruf önerileri
    private String dataSavingsHint;
    private String voiceSavingsHint;
    private String smsSavingsHint;
    
    // Günlük detaylar
    private List<UsageDTO> dailyUsage;
    
    // Fatura etkisi
    private BigDecimal estimatedDataCost;
    private BigDecimal estimatedVoiceCost;
    private BigDecimal estimatedSMSCost;
    private BigDecimal estimatedRoamingCost;
}
