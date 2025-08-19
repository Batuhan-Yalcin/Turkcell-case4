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
import com.turkcellcase4.common.exception.BusinessLogicException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyServiceImpl implements AnomalyService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;

    @Override
    public AnomalyResponseDTO detectAnomalies(AnomalyRequestDTO request) {
        log.info("Detecting anomalies for user: {} and period: {}", request.getUserId(), request.getPeriod());
        
        try {
            List<AnomalyDTO> anomalies = new ArrayList<>();
            
            // Parse period
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate periodDate = LocalDate.parse(request.getPeriod() + "-01", formatter);
            
            // Get current month bill
            int year = periodDate.getYear();
            int month = periodDate.getMonthValue();
            
            Optional<Bill> currentBill = billRepository.findByUserIdAndPeriod(request.getUserId(), year, month);
            if (currentBill.isEmpty()) {
                return AnomalyResponseDTO.builder().anomalies(anomalies).build();
            }
            
            // Get last 3 months bills for comparison
            LocalDate threeMonthsAgo = periodDate.minusMonths(3);
            List<Bill> recentBills = billRepository.findRecentBillsByUserId(request.getUserId(), threeMonthsAgo);
            
            // Detect anomalies
            anomalies.addAll(detectSpikeAnomalies(currentBill.get(), recentBills));
            anomalies.addAll(detectNewItemsAnomalies(currentBill.get(), recentBills));
            anomalies.addAll(detectRoamingAnomalies(currentBill.get(), recentBills));
            anomalies.addAll(detectPremiumSMSAnomalies(currentBill.get(), recentBills));
            
            return AnomalyResponseDTO.builder().anomalies(anomalies).build();
        } catch (Exception e) {
            throw new BusinessLogicException("Anomali tespiti hatası: " + e.getMessage());
        }
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
                    .category(ItemCategory.ONE_OFF.name())
                    .delta(difference)
                    .percentageChange(percentageChange)
                    .reason(String.format("Fatura tutarında %s%% artış", percentageChange.setScale(1, RoundingMode.HALF_UP)))
                    .suggestedAction("Fatura detaylarını inceleyin")
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
        
        if (previousBills.isEmpty()) return anomalies;
        
        // Batch query ile tüm bill item'ları tek seferde al
        List<Long> allBillIds = new ArrayList<>();
        allBillIds.add(currentBill.getBillId());
        allBillIds.addAll(previousBills.stream().map(Bill::getBillId).collect(Collectors.toList()));
        
        List<BillItem> allItems = billItemRepository.findByBillIdsIn(allBillIds);
        
        // Current bill items grouped by category
        Map<ItemCategory, List<BillItem>> currentItemsByCategory = allItems.stream()
                .filter(item -> item.getBill().getBillId().equals(currentBill.getBillId()))
                .collect(Collectors.groupingBy(BillItem::getCategory));
        
        // Previous bills items grouped by category
        Map<ItemCategory, List<BillItem>> previousItemsByCategory = allItems.stream()
                .filter(item -> !item.getBill().getBillId().equals(currentBill.getBillId()))
                .collect(Collectors.groupingBy(BillItem::getCategory));
        
        // Compare each category
        for (Map.Entry<ItemCategory, List<BillItem>> entry : currentItemsByCategory.entrySet()) {
            ItemCategory category = entry.getKey();
            List<BillItem> currentItems = entry.getValue();
            
            BigDecimal currentTotal = currentItems.stream()
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            List<BillItem> previousItems = previousItemsByCategory.getOrDefault(category, new ArrayList<>());
            BigDecimal previousTotal = previousItems.stream()
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (previousTotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal difference = currentTotal.subtract(previousTotal);
                BigDecimal percentageChange = difference.divide(previousTotal, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                
                // Detect significant changes (>50% increase or decrease)
                if (percentageChange.abs().compareTo(new BigDecimal("50")) > 0) {
                    anomalies.add(AnomalyDTO.builder()
                            .type(percentageChange.compareTo(BigDecimal.ZERO) > 0 ? AnomalyType.SPIKE : AnomalyType.NEW_ITEM)
                            .category(category.name())
                            .delta(difference.abs())
                            .percentageChange(percentageChange.abs())
                            .reason(String.format("%s kategorisinde %s%% %s", 
                                    category.name().toLowerCase(), 
                                    percentageChange.abs().setScale(1, RoundingMode.HALF_UP),
                                    percentageChange.compareTo(BigDecimal.ZERO) > 0 ? "artış" : "azalış"))
                            .suggestedAction("Bu kategorideki değişikliği kontrol edin")
                            .build());
                }
            }
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectNewItemsAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) return anomalies;
        
        // Batch query ile tüm bill item'ları tek seferde al
        List<Long> allBillIds = new ArrayList<>();
        allBillIds.add(currentBill.getBillId());
        allBillIds.addAll(previousBills.stream().map(Bill::getBillId).collect(Collectors.toList()));
        
        List<BillItem> allItems = billItemRepository.findByBillIdsIn(allBillIds);
        
        // Get all item subtypes from previous bills
        Set<String> previousSubtypes = allItems.stream()
                .filter(item -> !item.getBill().getBillId().equals(currentBill.getBillId()))
                .map(BillItem::getSubtype)
                .collect(Collectors.toSet());
        
        // Check for new subtypes in current bill
        List<BillItem> currentItems = allItems.stream()
                .filter(item -> item.getBill().getBillId().equals(currentBill.getBillId()))
                .collect(Collectors.toList());
        
        for (BillItem item : currentItems) {
            if (!previousSubtypes.contains(item.getSubtype())) {
                anomalies.add(AnomalyDTO.builder()
                        .category(item.getCategory().name())
                        .subtype(item.getSubtype())
                        .delta(item.getAmount())
                        .percentageChange(BigDecimal.valueOf(100))
                        .reason("Bu kalem ilk kez görüldü")
                        .suggestedAction("Kalemin neden eklendiğini kontrol edin")
                        .type(AnomalyType.NEW_ITEM)
                        .build());
            }
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectRoamingAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) return anomalies;
        
        // Batch query ile tüm bill item'ları tek seferde al
        List<Long> allBillIds = new ArrayList<>();
        allBillIds.add(currentBill.getBillId());
        allBillIds.addAll(previousBills.stream().map(Bill::getBillId).collect(Collectors.toList()));
        
        List<BillItem> allItems = billItemRepository.findByBillIdsIn(allBillIds);
        
        // Check if roaming was activated
        List<BillItem> currentRoamingItems = allItems.stream()
                .filter(item -> item.getBill().getBillId().equals(currentBill.getBillId()))
                .filter(item -> item.getCategory() == ItemCategory.ROAMING)
                .collect(Collectors.toList());
        
        if (!currentRoamingItems.isEmpty()) {
            // Check if there was roaming in previous months
            boolean hadRoamingBefore = allItems.stream()
                    .filter(item -> !item.getBill().getBillId().equals(currentBill.getBillId()))
                    .anyMatch(item -> item.getCategory() == ItemCategory.ROAMING);
            
            if (!hadRoamingBefore) {
                BigDecimal totalRoaming = currentRoamingItems.stream()
                        .map(BillItem::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                anomalies.add(AnomalyDTO.builder()
                        .category(ItemCategory.ROAMING.name())
                        .subtype("roaming_activation")
                        .delta(totalRoaming)
                        .percentageChange(BigDecimal.valueOf(100))
                        .reason("Roaming servisi bu ay aktif edildi")
                        .suggestedAction("Roaming kullanımını kontrol edin ve gerekirse kapatın")
                        .type(AnomalyType.ROAMING_ACTIVATION)
                        .build());
            }
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectPremiumSMSAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) return anomalies;
        
        // Batch query ile tüm bill item'ları tek seferde al
        List<Long> allBillIds = new ArrayList<>();
        allBillIds.add(currentBill.getBillId());
        allBillIds.addAll(previousBills.stream().map(Bill::getBillId).collect(Collectors.toList()));
        
        List<BillItem> allItems = billItemRepository.findByBillIdsIn(allBillIds);
        
        // Calculate average Premium SMS amount from previous months
        List<BigDecimal> previousPremiumSMSAmounts = allItems.stream()
                .filter(item -> !item.getBill().getBillId().equals(currentBill.getBillId()))
                .filter(item -> item.getCategory() == ItemCategory.PREMIUM_SMS)
                .collect(Collectors.groupingBy(item -> item.getBill().getBillId()))
                .values()
                .stream()
                .map(items -> items.stream()
                        .map(BillItem::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .collect(Collectors.toList());
        
        BigDecimal averagePremiumSMS = previousPremiumSMSAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(previousPremiumSMSAmounts.size()), 2, RoundingMode.HALF_UP);
        
        // Get current Premium SMS amount
        BigDecimal currentPremiumSMS = allItems.stream()
                .filter(item -> item.getBill().getBillId().equals(currentBill.getBillId()))
                .filter(item -> item.getCategory() == ItemCategory.PREMIUM_SMS)
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Check for significant increase (more than 80% increase)
        if (averagePremiumSMS.compareTo(BigDecimal.ZERO) > 0 && currentPremiumSMS.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal increase = currentPremiumSMS.subtract(averagePremiumSMS);
            BigDecimal percentageIncrease = increase.divide(averagePremiumSMS, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            if (percentageIncrease.compareTo(BigDecimal.valueOf(80)) > 0) {
                anomalies.add(AnomalyDTO.builder()
                        .category(ItemCategory.PREMIUM_SMS.name())
                        .subtype("premium_sms_increase")
                        .delta(increase)
                        .percentageChange(percentageIncrease)
                        .reason(String.format("Premium SMS ücreti %s%% arttı", percentageIncrease))
                        .suggestedAction("Premium SMS kullanımını kontrol edin ve gerekirse engelleyin")
                        .type(AnomalyType.PREMIUM_SMS_INCREASE)
                        .build());
            }
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectVASAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) return anomalies;
        
        // Batch query ile tüm bill item'ları tek seferde al
        List<Long> allBillIds = new ArrayList<>();
        allBillIds.add(currentBill.getBillId());
        allBillIds.addAll(previousBills.stream().map(Bill::getBillId).collect(Collectors.toList()));
        
        List<BillItem> allItems = billItemRepository.findByBillIdsIn(allBillIds);
        
        // Get current VAS items (excluding plan fee)
        List<BillItem> currentVASItems = allItems.stream()
                .filter(item -> item.getBill().getBillId().equals(currentBill.getBillId()))
                .filter(item -> item.getCategory() == ItemCategory.VAS && !"plan_fee".equals(item.getSubtype()))
                .collect(Collectors.toList());
        
        // Get previous VAS items
        Set<String> previousVASSubtypes = allItems.stream()
                .filter(item -> !item.getBill().getBillId().equals(currentBill.getBillId()))
                .filter(item -> item.getCategory() == ItemCategory.VAS && !"plan_fee".equals(item.getSubtype()))
                .map(BillItem::getSubtype)
                .collect(Collectors.toSet());
        
        // Check for new VAS services
        for (BillItem item : currentVASItems) {
            if (!previousVASSubtypes.contains(item.getSubtype())) {
                anomalies.add(AnomalyDTO.builder()
                        .category(ItemCategory.VAS.name())
                        .subtype(item.getSubtype())
                        .delta(item.getAmount())
                        .percentageChange(BigDecimal.valueOf(100))
                        .reason("Yeni VAS servisi aktif edildi: " + item.getDescription())
                        .suggestedAction("Bu servisi gerçekten kullanıyor musunuz? Kontrol edin")
                        .type(AnomalyType.NEW_ITEM)
                        .build());
            }
        }
        
        return anomalies;
    }

    private List<AnomalyDTO> detectSpikeAnomalies(Bill currentBill, List<Bill> previousBills) {
        List<AnomalyDTO> anomalies = new ArrayList<>();
        
        if (previousBills.isEmpty()) return anomalies;
        
        // Calculate average and standard deviation for total amount
        List<BigDecimal> amounts = previousBills.stream()
                .map(Bill::getTotalAmount)
                .collect(Collectors.toList());
        
        BigDecimal average = amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal variance = amounts.stream()
                .map(amount -> amount.subtract(average).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        BigDecimal threshold = average.add(stdDev.multiply(BigDecimal.valueOf(2)));
        
        if (currentBill.getTotalAmount().compareTo(threshold) > 0) {
            BigDecimal delta = currentBill.getTotalAmount().subtract(average);
            BigDecimal percentageChange = delta.divide(average, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            
            anomalies.add(AnomalyDTO.builder()
                    .category(ItemCategory.ONE_OFF.name())
                    .subtype("total_amount_spike")
                    .delta(delta)
                    .percentageChange(percentageChange)
                    .reason(String.format("Önceki ortalama %s TL iken bu ay %s TL", average, currentBill.getTotalAmount()))
                    .suggestedAction("Fatura detaylarını inceleyerek artış nedenini bulun")
                    .type(AnomalyType.SPIKE)
                    .build());
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
        LocalDate startDate = LocalDate.now().minusMonths(months);
        return billRepository.findRecentBillsByUserId(userId, startDate);
    }
}
