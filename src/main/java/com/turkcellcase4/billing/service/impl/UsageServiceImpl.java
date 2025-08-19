package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.dto.UsageDTO;
import com.turkcellcase4.billing.dto.UsageSummaryDTO;
import com.turkcellcase4.billing.model.UsageDaily;
import com.turkcellcase4.billing.repository.UsageDailyRepository;
import com.turkcellcase4.billing.service.UsageService;
import com.turkcellcase4.common.exception.BusinessLogicException;
import com.turkcellcase4.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageServiceImpl implements UsageService {

    private final UsageDailyRepository usageDailyRepository;

    @Override
    public List<UsageDTO> getDailyUsage(Long userId, String period) {
        log.info("Getting daily usage for user: {} and period: {}", userId, period);
        
        try {
            LocalDate startDate = parsePeriodToStartDate(period);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            
            return getDailyUsageByDateRange(userId, startDate, endDate);
        } catch (Exception e) {
            throw new BusinessLogicException("Günlük kullanım verileri alınırken hata: " + e.getMessage());
        }
    }

    @Override
    public List<UsageDTO> getDailyUsageByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting daily usage for user: {} between {} and {}", userId, startDate, endDate);
        
        List<UsageDaily> usageData = usageDailyRepository.findByUser_UserIdAndDateBetween(userId, startDate, endDate);
        
        return usageData.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UsageSummaryDTO getUsageSummary(Long userId, String period) {
        log.info("Getting usage summary for user: {} and period: {}", userId, period);
        
        try {
            LocalDate startDate = parsePeriodToStartDate(period);
            LocalDate endDate = startDate.plusMonths(1).minusDays(1);
            
            return getUsageSummaryByDateRange(userId, startDate, endDate);
        } catch (Exception e) {
            throw new BusinessLogicException("Kullanım özeti alınırken hata: " + e.getMessage());
        }
    }

    @Override
    public UsageSummaryDTO getUsageSummaryByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting usage summary for user: {} between {} and {}", userId, startDate, endDate);
        
        List<UsageDaily> usageData = usageDailyRepository.findByUser_UserIdAndDateBetween(userId, startDate, endDate);
        
        if (usageData.isEmpty()) {
            throw new ResourceNotFoundException("Kullanıcı için belirtilen dönemde kullanım verisi bulunamadı");
        }
        
        return buildUsageSummary(userId, startDate, endDate, usageData);
    }

    @Override
    public UsageSummaryDTO analyzeUsageTrend(Long userId, int months) {
        log.info("Analyzing usage trend for user: {} for last {} months", userId, months);
        
        LocalDate startDate = LocalDate.now().minusMonths(months);
        LocalDate endDate = LocalDate.now();
        
        List<UsageDaily> usageData = usageDailyRepository.findByUser_UserIdAndDateGreaterThanEqual(userId, startDate);
        
        if (usageData.isEmpty()) {
            throw new ResourceNotFoundException("Kullanıcı için belirtilen dönemde kullanım verisi bulunamadı");
        }
        
        return buildUsageSummary(userId, startDate, endDate, usageData);
    }

    @Override
    public String getDataUsageAnalysis(Long userId, String period) {
        log.info("Getting data usage analysis for user: {} and period: {}", userId, period);
        
        UsageSummaryDTO summary = getUsageSummary(userId, period);
        
        if (summary.getTotalDataGB() == null || summary.getTotalDataGB() == 0) {
            return "Bu dönemde data kullanımı bulunmuyor.";
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("Bu ay toplam %.1f GB data kullandınız. ", summary.getTotalDataGB()));
        
        if (summary.getAverageDailyDataGB() != null) {
            analysis.append(String.format("Günlük ortalama %.2f GB. ", summary.getAverageDailyDataGB()));
        }
        
        if (summary.getDataSavingsHint() != null) {
            analysis.append(summary.getDataSavingsHint());
        }
        
        return analysis.toString();
    }

    @Override
    public String getVoiceUsageAnalysis(Long userId, String period) {
        log.info("Getting voice usage analysis for user: {} and period: {}", userId, period);
        
        UsageSummaryDTO summary = getUsageSummary(userId, period);
        
        if (summary.getTotalVoiceMinutes() == null || summary.getTotalVoiceMinutes() == 0) {
            return "Bu dönemde ses kullanımı bulunmuyor.";
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("Bu ay toplam %d dakika arama yaptınız. ", summary.getTotalVoiceMinutes()));
        
        if (summary.getAverageDailyVoiceMinutes() != null) {
            analysis.append(String.format("Günlük ortalama %d dakika. ", summary.getAverageDailyVoiceMinutes()));
        }
        
        if (summary.getVoiceSavingsHint() != null) {
            analysis.append(summary.getVoiceSavingsHint());
        }
        
        return analysis.toString();
    }

    @Override
    public String getSMSUsageAnalysis(Long userId, String period) {
        log.info("Getting SMS usage analysis for user: {} and period: {}", userId, period);
        
        UsageSummaryDTO summary = getUsageSummary(userId, period);
        
        if (summary.getTotalSMSCount() == null || summary.getTotalSMSCount() == 0) {
            return "Bu dönemde SMS kullanımı bulunmuyor.";
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("Bu ay toplam %d SMS gönderdiniz. ", summary.getTotalSMSCount()));
        
        if (summary.getAverageDailySMSCount() != null) {
            analysis.append(String.format("Günlük ortalama %d SMS. ", summary.getAverageDailySMSCount()));
        }
        
        if (summary.getSmsSavingsHint() != null) {
            analysis.append(summary.getSmsSavingsHint());
        }
        
        return analysis.toString();
    }

    @Override
    public String getRoamingUsageAnalysis(Long userId, String period) {
        log.info("Getting roaming usage analysis for user: {} and period: {}", userId, period);
        
        UsageSummaryDTO summary = getUsageSummary(userId, period);
        
        if (summary.getTotalRoamingGB() == null || summary.getTotalRoamingGB() == 0) {
            return "Bu dönemde roaming kullanımı bulunmuyor.";
        }
        
        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("Bu ay toplam %.2f GB yurt dışı data kullandınız. ", summary.getTotalRoamingGB()));
        
        if (summary.getAverageDailyRoamingGB() != null) {
            analysis.append(String.format("Günlük ortalama %.2f GB. ", summary.getAverageDailyRoamingGB()));
        }
        
        if (summary.getDataSavingsHint() != null) {
            analysis.append(summary.getDataSavingsHint());
        }
        
        return analysis.toString();
    }

    private UsageSummaryDTO buildUsageSummary(Long userId, LocalDate startDate, LocalDate endDate, List<UsageDaily> usageData) {
        // Toplam kullanım hesaplamaları
        Double totalDataMB = usageData.stream()
                .mapToDouble(ud -> ud.getMbUsed() != null ? ud.getMbUsed() : 0.0)
                .sum();
        
        Integer totalVoiceMinutes = usageData.stream()
                .mapToInt(ud -> ud.getMinutesUsed() != null ? ud.getMinutesUsed() : 0)
                .sum();
        
        Integer totalSMSCount = usageData.stream()
                .mapToInt(ud -> ud.getSmsUsed() != null ? ud.getSmsUsed() : 0)
                .sum();
        
        Double totalRoamingMB = usageData.stream()
                .mapToDouble(ud -> ud.getRoamingMb() != null ? ud.getRoamingMb() : 0.0)
                .sum();
        
        // Ortalama günlük kullanım
        long daysCount = startDate.until(endDate.plusDays(1)).getDays();
        Double averageDailyDataGB = totalDataMB / 1024.0 / daysCount;
        Integer averageDailyVoiceMinutes = totalVoiceMinutes / (int) daysCount;
        Integer averageDailySMSCount = totalSMSCount / (int) daysCount;
        Double averageDailyRoamingGB = totalRoamingMB / 1024.0 / daysCount;
        
        // En yüksek kullanım günleri
        LocalDate peakDataDate = usageData.stream()
                .max(Comparator.comparing(ud -> ud.getMbUsed() != null ? ud.getMbUsed() : 0.0))
                .map(UsageDaily::getDate)
                .orElse(null);
        
        LocalDate peakVoiceDate = usageData.stream()
                .max(Comparator.comparing(ud -> ud.getMinutesUsed() != null ? ud.getMinutesUsed() : 0))
                .map(UsageDaily::getDate)
                .orElse(null);
        
        LocalDate peakSMSDate = usageData.stream()
                .max(Comparator.comparing(ud -> ud.getSmsUsed() != null ? ud.getSmsUsed() : 0))
                .map(UsageDaily::getDate)
                .orElse(null);
        
        // Kullanım trendi analizi
        String dataTrend = analyzeTrend(usageData, UsageDaily::getMbUsed);
        String voiceTrend = analyzeTrend(usageData, UsageDaily::getMinutesUsed);
        String smsTrend = analyzeTrend(usageData, UsageDaily::getSmsUsed);
        
        // Tasarruf önerileri
        String dataSavingsHint = generateDataSavingsHint(totalDataMB);
        String voiceSavingsHint = generateVoiceSavingsHint(totalVoiceMinutes);
        String smsSavingsHint = generateSMSSavingsHint(totalSMSCount);
        
        // Günlük detaylar
        List<UsageDTO> dailyUsage = usageData.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // Fatura etkisi (basit hesaplama)
        BigDecimal estimatedDataCost = calculateEstimatedDataCost(totalDataMB);
        BigDecimal estimatedVoiceCost = calculateEstimatedVoiceCost(totalVoiceMinutes);
        BigDecimal estimatedSMSCost = calculateEstimatedSMSCost(totalSMSCount);
        BigDecimal estimatedRoamingCost = calculateEstimatedRoamingCost(totalRoamingMB);
        
        return UsageSummaryDTO.builder()
                .userId(userId)
                .period(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .startDate(startDate)
                .endDate(endDate)
                .totalDataGB(totalDataMB / 1024.0)
                .totalVoiceMinutes(totalVoiceMinutes)
                .totalSMSCount(totalSMSCount)
                .totalRoamingGB(totalRoamingMB / 1024.0)
                .averageDailyDataGB(averageDailyDataGB)
                .averageDailyVoiceMinutes(averageDailyVoiceMinutes)
                .averageDailySMSCount(averageDailySMSCount)
                .averageDailyRoamingGB(averageDailyRoamingGB)
                .peakDataDate(peakDataDate)
                .peakVoiceDate(peakVoiceDate)
                .peakSMSDate(peakSMSDate)
                .dataTrend(dataTrend)
                .voiceTrend(voiceTrend)
                .smsTrend(smsTrend)
                .dataSavingsHint(dataSavingsHint)
                .voiceSavingsHint(voiceSavingsHint)
                .smsSavingsHint(smsSavingsHint)
                .dailyUsage(dailyUsage)
                .estimatedDataCost(estimatedDataCost)
                .estimatedVoiceCost(estimatedVoiceCost)
                .estimatedSMSCost(estimatedSMSCost)
                .estimatedRoamingCost(estimatedRoamingCost)
                .build();
    }

    private UsageDTO convertToDTO(UsageDaily usageDaily) {
        return UsageDTO.builder()
                .id(usageDaily.getId())
                .userId(usageDaily.getUser().getUserId())
                .date(usageDaily.getDate())
                .mbUsed(usageDaily.getMbUsed())
                .minutesUsed(usageDaily.getMinutesUsed())
                .smsUsed(usageDaily.getSmsUsed())
                .roamingMb(usageDaily.getRoamingMb())
                .gbUsed(usageDaily.getMbUsed() != null ? usageDaily.getMbUsed() / 1024.0 : 0.0)
                .formattedDate(usageDaily.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .build();
    }

    private LocalDate parsePeriodToStartDate(String period) {
        try {
            // Period formatı "yyyy-MM" şeklinde geliyor, "-01" ekleyerek tam tarih yapıyoruz
            String fullDate = period + "-01";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(fullDate, formatter);
        } catch (Exception e) {
            throw new BusinessLogicException("Geçersiz period formatı: " + period + ". Beklenen format: yyyy-MM");
        }
    }

    private <T extends Number> String analyzeTrend(List<UsageDaily> usageData, java.util.function.Function<UsageDaily, T> extractor) {
        if (usageData.size() < 2) return "STABLE";
        
        List<T> values = usageData.stream()
                .map(extractor)
                .filter(v -> v != null)
                .collect(Collectors.toList());
        
        if (values.size() < 2) return "STABLE";
        
        // Basit trend analizi: ilk yarı vs son yarı
        int midPoint = values.size() / 2;
        double firstHalfAvg = values.subList(0, midPoint).stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0.0);
        
        double secondHalfAvg = values.subList(midPoint, values.size()).stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0.0);
        
        double change = ((secondHalfAvg - firstHalfAvg) / firstHalfAvg) * 100;
        
        if (change > 10) return "INCREASING";
        if (change < -10) return "DECREASING";
        return "STABLE";
    }

    private String generateDataSavingsHint(Double totalDataMB) {
        if (totalDataMB == null || totalDataMB < 1024) return null;
        
        double totalGB = totalDataMB / 1024.0;
        if (totalGB > 10) {
            return "Yüksek data kullanımı. Daha büyük plana geçmeyi düşünebilirsiniz.";
        }
        return null;
    }

    private String generateVoiceSavingsHint(Integer totalVoiceMinutes) {
        if (totalVoiceMinutes == null || totalVoiceMinutes < 500) return null;
        
        if (totalVoiceMinutes > 1000) {
            return "Yüksek ses kullanımı. Daha büyük plana geçmeyi düşünebilirsiniz.";
        }
        return null;
    }

    private String generateSMSSavingsHint(Integer totalSMSCount) {
        if (totalSMSCount == null || totalSMSCount < 100) return null;
        
        if (totalSMSCount > 500) {
            return "Yüksek SMS kullanımı. Daha büyük plana geçmeyi düşünebilirsiniz.";
        }
        return null;
    }

    private BigDecimal calculateEstimatedDataCost(Double totalDataMB) {
        if (totalDataMB == null || totalDataMB <= 0) return BigDecimal.ZERO;
        
        // Basit hesaplama: 1GB = 0.50 TL (örnek)
        double totalGB = totalDataMB / 1024.0;
        return BigDecimal.valueOf(totalGB * 0.50).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEstimatedVoiceCost(Integer totalVoiceMinutes) {
        if (totalVoiceMinutes == null || totalVoiceMinutes <= 0) return BigDecimal.ZERO;
        
        // Basit hesaplama: 1 dakika = 0.25 TL (örnek)
        return BigDecimal.valueOf(totalVoiceMinutes * 0.25).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEstimatedSMSCost(Integer totalSMSCount) {
        if (totalSMSCount == null || totalSMSCount <= 0) return BigDecimal.ZERO;
        
        // Basit hesaplama: 1 SMS = 0.10 TL (örnek)
        return BigDecimal.valueOf(totalSMSCount * 0.10).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEstimatedRoamingCost(Double totalRoamingMB) {
        if (totalRoamingMB == null || totalRoamingMB <= 0) return BigDecimal.ZERO;
        
        // Basit hesaplama: 1MB = 0.01 TL (örnek)
        return BigDecimal.valueOf(totalRoamingMB * 0.01).setScale(2, RoundingMode.HALF_UP);
    }
}
