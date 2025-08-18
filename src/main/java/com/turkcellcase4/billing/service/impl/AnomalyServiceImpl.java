package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.dto.*;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.repository.BillItemRepository;
import com.turkcellcase4.billing.service.AnomalyService;
import com.turkcellcase4.common.enums.AnomalyType;
import com.turkcellcase4.common.enums.ItemCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyServiceImpl implements AnomalyService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;

    @Override
    public AnomalyResponseDTO detectAnomalies(AnomalyRequestDTO request) {
        log.info("Detecting anomalies for user: {} and period: {}", request.getUserId(), request.getPeriod());
        
        // Get current period bill
        Bill currentBill = getCurrentBill(request.getUserId(), request.getPeriod());
        if (currentBill == null) {
            throw new RuntimeException("No bill found for the specified period");
        }
        
        // Get last 3 months bills for comparison
        List<Bill> previousBills = getPreviousBills(request.getUserId(), request.getPeriod(), 3);
        
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        // Detect total amount anomalies
        anomalies.addAll(detectTotalAmountAnomalies(currentBill, previousBills));
        
        // Detect category-based anomalies
        anomalies.addAll(detectCategoryAnomalies(currentBill, previousBills));
        
        // Detect new items anomalies
        anomalies.addAll(detectNewItemsAnomalies(currentBill, previousBills));
        
        // Detect roaming anomalies
        anomalies.addAll(detectRoamingAnomalies(currentBill, previousBills));
        
        // Detect premium SMS anomalies
        anomalies.addAll(detectPremiumSMSAnomalies(currentBill, previousBills));
        
        // Detect VAS anomalies
        anomalies.addAll(detectVASAnomalies(currentBill, previousBills));
        
        return AnomalyResponseDTO.builder()
                .anomalies(anomalies)
                .totalAnomalies(anomalies.size())
                .period(request.getPeriod())
                .userId(request.getUserId())
                .build();
    }

    @Override
    public AnomalyResponseDTO getAnomalyHistory(Long userId) {
        log.info("Getting anomaly history for user: {}", userId);
        
        // Get last 6 months bills
        List<Bill> bills = getLastMonthsBills(userId, 6);
        List<AnomalyDTO> historicalAnomalies = new ArrayList<>();
        
        for (Bill bill : bills) {
            String period = bill.getPeriodStart().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            List<Bill> previousBills = bills.stream()
                    .filter(b -> b.getPeriodStart().isBefore(bill.getPeriodStart()))
                    .limit(3)
                    .collect(Collectors.toList());
            
            if (!previousBills.isEmpty()) {
                historicalAnomalies.addAll(detectTotalAmountAnomalies(bill, previousBills));
            }
        }
        
        return AnomalyResponseDTO.builder()
                .anomalies(historicalAnomalies)
                .totalAnomalies(historicalAnomalies.size())
                .userId(userId)
                .build();
    }

    @Override
    public AnomalyResponseDTO getAnomalySummary(Long userId) {
        log.info("Getting anomaly summary for user: {}", userId);
        
        // Get last 3 months anomalies
        List<Bill> recentBills = getLastMonthsBills(userId, 3);
        List<AnomalyDTO> recentAnomalies = new ArrayList<>();
        
        for (Bill bill : recentBills) {
            String period = bill.getPeriodStart().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            List<Bill> previousBills = recentBills.stream()
                    .filter(b -> b.getPeriodStart().isBefore(bill.getPeriodStart()))
                    .limit(2)
                    .collect(Collectors.toList());
            
            if (!previousBills.isEmpty()) {
                recentAnomalies.addAll(detectTotalAmountAnomalies(bill, previousBills));
            }
        }
        
        // Group anomalies by type
        Map<AnomalyType, Long> anomalyCounts = recentAnomalies.stream()
                .collect(Collectors.groupingBy(AnomalyDTO::getType, Collectors.counting()));
        
        return AnomalyResponseDTO.builder()
                .anomalies(recentAnomalies)
                .totalAnomalies(recentAnomalies.size())
                .userId(userId)
                .anomalySummary(anomalyCounts)
                .build();
    }

    private List<AnomalyDTO> detectTotalAmountAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) {
            return anomalies;
        }
        
        // Calculate statistics from previous bills
        BigDecimal previousTotal = previousBills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(previousBills.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal currentTotal = currentBill.getTotalAmount();
        BigDecimal difference = currentTotal.subtract(previousTotal);
        BigDecimal percentageChange = difference.divide(previousTotal, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        
        // Detect spike anomalies (>80% increase)
        if (percentageChange.compareTo(new BigDecimal("80")) > 0) {
            anomalies.add(AnomalyDTO.builder()
                    .type(AnomalyType.SPIKE)
                    .category("total_amount")
                    .delta(difference)
                    .percentageChange(percentageChange)
                    .reason(String.format("Önceki ortalama %.2f TL iken bu ay %.2f TL (%%%.1f artış)", 
                            previousTotal, currentTotal, percentageChange))
                    .suggestedAction("Fatura detaylarını inceleyin ve beklenmedik kalemleri kontrol edin")
                    .severity("HIGH")
                    .build());
        }
        
        // Detect z-score anomalies (>2 standard deviations)
        BigDecimal variance = previousBills.stream()
                .map(bill -> bill.getTotalAmount().subtract(previousTotal).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(previousBills.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal standardDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        BigDecimal zScore = difference.divide(standardDeviation, 2, RoundingMode.HALF_UP);
        
        if (zScore.abs().compareTo(new BigDecimal("2")) > 0) {
            anomalies.add(AnomalyDTO.builder()
                    .type(AnomalyType.STATISTICAL)
                    .category("total_amount")
                    .delta(difference)
                    .zScore(zScore)
                    .reason(String.format("Z-score: %.2f (normal aralık: -2 ile +2 arası)", zScore))
                    .suggestedAction("İstatistiksel olarak anormal bir artış tespit edildi")
                    .severity("MEDIUM")
                    .build());
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectCategoryAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) {
            return anomalies;
        }
        
        // Get current bill items by category
        List<BillItem> currentItems = billItemRepository.findByBill_BillId(currentBill.getBillId());
        Map<ItemCategory, BigDecimal> currentCategoryTotals = currentItems.stream()
                .collect(Collectors.groupingBy(
                    BillItem::getCategory,
                    Collectors.reducing(BigDecimal.ZERO, BillItem::getAmount, BigDecimal::add)
                ));
        
        // Calculate previous category averages
        Map<ItemCategory, BigDecimal> previousCategoryAverages = new HashMap<>();
        for (ItemCategory category : ItemCategory.values()) {
            BigDecimal total = BigDecimal.ZERO;
            int count = 0;
            
            for (Bill bill : previousBills) {
                List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
                BigDecimal categoryTotal = items.stream()
                        .filter(item -> category.equals(item.getCategory()))
                        .map(BillItem::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (categoryTotal.compareTo(BigDecimal.ZERO) > 0) {
                    total = total.add(categoryTotal);
                    count++;
                }
            }
            
            if (count > 0) {
                previousCategoryAverages.put(category, total.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP));
            }
        }
        
        // Detect category anomalies
        for (Map.Entry<ItemCategory, BigDecimal> entry : currentCategoryTotals.entrySet()) {
            ItemCategory category = entry.getKey();
            BigDecimal currentAmount = entry.getValue();
            BigDecimal previousAverage = previousCategoryAverages.get(category);
            
            if (previousAverage != null && previousAverage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal difference = currentAmount.subtract(previousAverage);
                BigDecimal percentageChange = difference.divide(previousAverage, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                
                // Detect significant increases (>100% for most categories, >50% for premium services)
                BigDecimal threshold = (category == ItemCategory.PREMIUM_SMS || category == ItemCategory.VAS) 
                        ? new BigDecimal("50") : new BigDecimal("100");
                
                if (percentageChange.compareTo(threshold) > 0) {
                    anomalies.add(AnomalyDTO.builder()
                            .type(AnomalyType.CATEGORY_SPIKE)
                            .category(category.name().toLowerCase())
                            .delta(difference)
                            .percentageChange(percentageChange)
                            .reason(String.format("%s kategorisinde önceki ortalama %.2f TL iken bu ay %.2f TL (%%%.1f artış)", 
                                    category.name().toLowerCase(), previousAverage, currentAmount, percentageChange))
                            .suggestedAction(String.format("%s kullanımınızı gözden geçirin", category.name().toLowerCase()))
                            .severity("MEDIUM")
                            .build());
                }
            }
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectNewItemsAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) {
            return anomalies;
        }
        
        // Get current bill items
        List<BillItem> currentItems = billItemRepository.findByBill_BillId(currentBill.getBillId());
        
        // Get all previous bill items
        Set<String> previousItemTypes = new HashSet<>();
        for (Bill bill : previousBills) {
            List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
            for (BillItem item : items) {
                previousItemTypes.add(item.getSubtype());
            }
        }
        
        // Detect new item types
        for (BillItem currentItem : currentItems) {
            if (!previousItemTypes.contains(currentItem.getSubtype())) {
                anomalies.add(AnomalyDTO.builder()
                        .type(AnomalyType.NEW_ITEM)
                        .category(currentItem.getCategory().name().toLowerCase())
                        .subtype(currentItem.getSubtype())
                        .delta(currentItem.getAmount())
                        .reason(String.format("İlk kez görülen kalem: %s (%.2f TL)", 
                                currentItem.getDescription(), currentItem.getAmount()))
                        .suggestedAction("Bu kalemin neden oluştuğunu kontrol edin")
                        .severity("LOW")
                        .build());
            }
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectRoamingAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) {
            return anomalies;
        }
        
        // Check if roaming was activated this month
        List<BillItem> currentItems = billItemRepository.findByBill_BillId(currentBill.getBillId());
        BigDecimal currentRoaming = currentItems.stream()
                .filter(item -> ItemCategory.ROAMING.equals(item.getCategory()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Check if there was no roaming in previous months
        boolean hadRoamingBefore = false;
        for (Bill bill : previousBills) {
            List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
            BigDecimal roamingAmount = items.stream()
                    .filter(item -> ItemCategory.ROAMING.equals(item.getCategory()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (roamingAmount.compareTo(BigDecimal.ZERO) > 0) {
                hadRoamingBefore = true;
                break;
            }
        }
        
        if (currentRoaming.compareTo(BigDecimal.ZERO) > 0 && !hadRoamingBefore) {
            anomalies.add(AnomalyDTO.builder()
                    .type(AnomalyType.ROAMING_ACTIVATION)
                    .category("roaming")
                    .delta(currentRoaming)
                    .reason("Yeni roaming aktivasyonu tespit edildi")
                    .suggestedAction("Roaming kullanımınızı kontrol edin ve gerekirse kapatın")
                    .severity("MEDIUM")
                    .build());
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectPremiumSMSAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) {
            return anomalies;
        }
        
        // Calculate current premium SMS total
        List<BillItem> currentItems = billItemRepository.findByBill_BillId(currentBill.getBillId());
        BigDecimal currentPremiumSMS = currentItems.stream()
                .filter(item -> ItemCategory.PREMIUM_SMS.equals(item.getCategory()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate previous premium SMS average
        BigDecimal previousPremiumSMS = BigDecimal.ZERO;
        int count = 0;
        
        for (Bill bill : previousBills) {
            List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
            BigDecimal premiumSMSTotal = items.stream()
                    .filter(item -> ItemCategory.PREMIUM_SMS.equals(item.getCategory()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (premiumSMSTotal.compareTo(BigDecimal.ZERO) > 0) {
                previousPremiumSMS = previousPremiumSMS.add(premiumSMSTotal);
                count++;
            }
        }
        
        if (count > 0 && currentPremiumSMS.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal previousAverage = previousPremiumSMS.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
            BigDecimal difference = currentPremiumSMS.subtract(previousAverage);
            BigDecimal percentageChange = difference.divide(previousAverage, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            
            // Detect significant increase (>80%)
            if (percentageChange.compareTo(new BigDecimal("80")) > 0) {
                anomalies.add(AnomalyDTO.builder()
                        .type(AnomalyType.PREMIUM_SMS_INCREASE)
                        .category("premium_sms")
                        .delta(difference)
                        .percentageChange(percentageChange)
                        .reason(String.format("Premium SMS ücreti önceki ortalama %.2f TL iken bu ay %.2f TL (%%%.1f artış)", 
                                previousAverage, currentPremiumSMS, percentageChange))
                        .suggestedAction("Premium SMS kullanımınızı kontrol edin ve gerekirse engelleyin")
                        .severity("HIGH")
                        .build());
            }
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectVASAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) {
            return anomalies;
        }
        
        // Calculate current VAS total (excluding plan fee)
        List<BillItem> currentItems = billItemRepository.findByBill_BillId(currentBill.getBillId());
        BigDecimal currentVAS = currentItems.stream()
                .filter(item -> ItemCategory.VAS.equals(item.getCategory()) && !"plan_fee".equals(item.getSubtype()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate previous VAS average
        BigDecimal previousVAS = BigDecimal.ZERO;
        int count = 0;
        
        for (Bill bill : previousBills) {
            List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
            BigDecimal vasTotal = items.stream()
                    .filter(item -> ItemCategory.VAS.equals(item.getCategory()) && !"plan_fee".equals(item.getSubtype()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (vasTotal.compareTo(BigDecimal.ZERO) > 0) {
                previousVAS = previousVAS.add(vasTotal);
                count++;
            }
        }
        
        if (count > 0 && currentVAS.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal previousAverage = previousVAS.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
            BigDecimal difference = currentVAS.subtract(previousAverage);
            BigDecimal percentageChange = difference.divide(previousAverage, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            
            // Detect significant increase (>50%)
            if (percentageChange.compareTo(new BigDecimal("50")) > 0) {
                anomalies.add(AnomalyDTO.builder()
                        .type(AnomalyType.VAS_INCREASE)
                        .category("vas")
                        .delta(difference)
                        .percentageChange(percentageChange)
                        .reason(String.format("VAS ücreti önceki ortalama %.2f TL iken bu ay %.2f TL (%%%.1f artış)", 
                                previousAverage, currentVAS, percentageChange))
                        .suggestedAction("Kullanmadığınız VAS hizmetlerini iptal edin")
                        .severity("MEDIUM")
                        .build());
            }
        }
        
        return anomalies;
    }

    private Bill getCurrentBill(Long userId, String period) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate periodStart = LocalDate.parse(period + "-01", formatter);
        LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
        
        return billRepository.findByUser_UserIdAndPeriodStartBetween(userId, periodStart, periodEnd)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private List<Bill> getPreviousBills(Long userId, String period, int months) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate periodStart = LocalDate.parse(period + "-01", formatter);
        LocalDate startDate = periodStart.minusMonths(months);
        
        return billRepository.findByUser_UserIdAndPeriodStartBetween(userId, startDate, periodStart);
    }

    private List<Bill> getLastMonthsBills(Long userId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        
        return billRepository.findByUser_UserIdAndPeriodStartBetween(userId, startDate, endDate);
    }
}
