package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.UsageDTO;
import com.turkcellcase4.billing.dto.UsageSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface UsageService {
    
    /**
     * Kullanıcının belirli dönemdeki günlük kullanım verilerini getirir
     */
    List<UsageDTO> getDailyUsage(Long userId, String period);
    
    /**
     * Kullanıcının belirli tarih aralığındaki günlük kullanım verilerini getirir
     */
    List<UsageDTO> getDailyUsageByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Kullanıcının belirli dönemdeki kullanım özetini getirir
     */
    UsageSummaryDTO getUsageSummary(Long userId, String period);
    
    /**
     * Kullanıcının belirli tarih aralığındaki kullanım özetini getirir
     */
    UsageSummaryDTO getUsageSummaryByDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Kullanıcının son N ay kullanım trendini analiz eder
     */
    UsageSummaryDTO analyzeUsageTrend(Long userId, int months);
    
    /**
     * Kullanıcının data kullanımını analiz eder ve tasarruf önerileri sunar
     */
    String getDataUsageAnalysis(Long userId, String period);
    
    /**
     * Kullanıcının ses kullanımını analiz eder ve tasarruf önerileri sunar
     */
    String getVoiceUsageAnalysis(Long userId, String period);
    
    /**
     * Kullanıcının SMS kullanımını analiz eder ve tasarruf önerileri sunar
     */
    String getSMSUsageAnalysis(Long userId, String period);
    
    /**
     * Kullanıcının roaming kullanımını analiz eder ve tasarruf önerileri sunar
     */
    String getRoamingUsageAnalysis(Long userId, String period);
}
