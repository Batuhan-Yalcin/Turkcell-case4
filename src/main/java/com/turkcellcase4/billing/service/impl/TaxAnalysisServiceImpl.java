package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.service.TaxAnalysisService;
import com.turkcellcase4.billing.dto.TaxBreakdownDTO;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.repository.BillItemRepository;
import com.turkcellcase4.common.enums.ItemCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxAnalysisServiceImpl implements TaxAnalysisService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;

    @Override
    public TaxBreakdownDTO analyzeTaxBreakdown(Long billId) {
        log.info("Analyzing tax breakdown for billId: {}", billId);
        
        try {
            Bill bill = billRepository.findById(billId)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));
            
            List<BillItem> items = billItemRepository.findByBill_BillId(billId);
            
            // Calculate total amount
            BigDecimal totalAmount = bill.getTotalAmount();
            
            // Calculate total tax
            BigDecimal totalTax = items.stream()
                    .filter(item -> ItemCategory.TAX.equals(item.getCategory()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate effective tax rate
            BigDecimal effectiveTaxRate = totalAmount.compareTo(BigDecimal.ZERO) > 0 ?
                    totalTax.divide(totalAmount, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            // Calculate KDV amount (assuming KDV is 18%)
            BigDecimal kdvAmount = totalTax.multiply(new BigDecimal("0.18")).divide(new BigDecimal("0.18"), 2, RoundingMode.HALF_UP);
            
            // Calculate other taxes
            BigDecimal otherTaxes = totalTax.subtract(kdvAmount);
            
            // Build tax rates map
            Map<String, BigDecimal> taxRates = new HashMap<>();
            taxRates.put("KDV", new BigDecimal("0.18"));
            taxRates.put("ÖTV", BigDecimal.ZERO);
            taxRates.put("Diğer", otherTaxes);
            
            return TaxBreakdownDTO.builder()
                    .billId(billId)
                    .totalAmount(totalAmount)
                    .totalTax(totalTax)
                    .effectiveTaxRate(effectiveTaxRate)
                    .kdvAmount(kdvAmount)
                    .oivAmount(BigDecimal.ZERO)
                    .otherTaxes(otherTaxes)
                    .categoryTaxes(taxRates)
                    .build();
        } catch (Exception e) {
            log.error("Tax breakdown analysis error: {}", e.getMessage());
            Map<String, BigDecimal> taxRates = new HashMap<>();
            taxRates.put("KDV", new BigDecimal("0.18"));
            taxRates.put("ÖTV", new BigDecimal("0.00"));
            
            return TaxBreakdownDTO.builder()
                    .billId(billId)
                    .totalAmount(BigDecimal.ZERO)
                    .totalTax(BigDecimal.ZERO)
                    .effectiveTaxRate(new BigDecimal("0.18"))
                    .kdvAmount(BigDecimal.ZERO)
                    .oivAmount(BigDecimal.ZERO)
                    .otherTaxes(BigDecimal.ZERO)
                    .categoryTaxes(taxRates)
                    .build();
        }
    }

    @Override
    public TaxBreakdownDTO analyzeUserTaxTrend(Long userId, int months) {
        log.info("Analyzing user tax trend for userId: {} and months: {}", userId, months);
        
        try {
            LocalDate startDate = LocalDate.now().minusMonths(months);
            List<Bill> bills = billRepository.findRecentBillsByUserId(userId, startDate);
            
            if (bills.isEmpty()) {
                Map<String, BigDecimal> taxRates = new HashMap<>();
                taxRates.put("KDV", new BigDecimal("0.18"));
                
                return TaxBreakdownDTO.builder()
                        .userId(userId)
                        .totalAmount(BigDecimal.ZERO)
                        .totalTax(BigDecimal.ZERO)
                        .effectiveTaxRate(new BigDecimal("0.18"))
                        .kdvAmount(BigDecimal.ZERO)
                        .oivAmount(BigDecimal.ZERO)
                        .otherTaxes(BigDecimal.ZERO)
                        .categoryTaxes(taxRates)
                        .build();
            }
            
            // Calculate total amounts
            BigDecimal totalAmount = bills.stream()
                    .map(Bill::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalTax = BigDecimal.ZERO;
            BigDecimal totalKdv = BigDecimal.ZERO;
            
            for (Bill bill : bills) {
                List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
                BigDecimal billTax = items.stream()
                        .filter(item -> ItemCategory.TAX.equals(item.getCategory()))
                        .map(BillItem::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalTax = totalTax.add(billTax);
                
                // Calculate KDV for this bill
                BigDecimal billKdv = billTax.multiply(new BigDecimal("0.18")).divide(new BigDecimal("0.18"), 2, RoundingMode.HALF_UP);
                totalKdv = totalKdv.add(billKdv);
            }
            
            // Calculate effective tax rate
            BigDecimal effectiveTaxRate = totalAmount.compareTo(BigDecimal.ZERO) > 0 ?
                    totalTax.divide(totalAmount, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            // Calculate other taxes
            BigDecimal otherTaxes = totalTax.subtract(totalKdv);
            
            // Build tax rates map
            Map<String, BigDecimal> taxRates = new HashMap<>();
            taxRates.put("KDV", new BigDecimal("0.18"));
            taxRates.put("ÖTV", BigDecimal.ZERO);
            taxRates.put("Diğer", otherTaxes);
            
            return TaxBreakdownDTO.builder()
                    .userId(userId)
                    .totalAmount(totalAmount)
                    .totalTax(totalTax)
                    .effectiveTaxRate(effectiveTaxRate)
                    .kdvAmount(totalKdv)
                    .oivAmount(BigDecimal.ZERO)
                    .otherTaxes(otherTaxes)
                    .categoryTaxes(taxRates)
                    .build();
        } catch (Exception e) {
            log.error("User tax trend analysis error: {}", e.getMessage());
            Map<String, BigDecimal> taxRates = new HashMap<>();
            taxRates.put("KDV", new BigDecimal("0.18"));
            
            return TaxBreakdownDTO.builder()
                    .userId(userId)
                    .totalAmount(BigDecimal.ZERO)
                    .totalTax(BigDecimal.ZERO)
                    .effectiveTaxRate(new BigDecimal("0.18"))
                    .kdvAmount(BigDecimal.ZERO)
                    .oivAmount(BigDecimal.ZERO)
                    .otherTaxes(BigDecimal.ZERO)
                    .categoryTaxes(taxRates)
                    .build();
        }
    }

    @Override
    public String getTaxOptimizationSuggestions(Long billId) {
        log.info("Getting tax optimization suggestions for billId: {}", billId);
        
        try {
            TaxBreakdownDTO taxBreakdown = analyzeTaxBreakdown(billId);
            
            StringBuilder suggestions = new StringBuilder();
            
            if (taxBreakdown.getEffectiveTaxRate().compareTo(new BigDecimal("0.20")) > 0) {
                suggestions.append("Vergi oranınız yüksek. ");
            }
            
            if (taxBreakdown.getTotalTax().compareTo(new BigDecimal("50")) > 0) {
                suggestions.append("Toplam vergi tutarınız yüksek. ");
            }
            
            if (taxBreakdown.getKdvAmount().compareTo(BigDecimal.ZERO) > 0) {
                suggestions.append("KDV oranı standart (%18). ");
            }
            
            if (suggestions.length() == 0) {
                suggestions.append("Vergi yapınız normal seviyede. ");
            }
            
            suggestions.append("Vergi optimizasyonu için muhasebe uzmanınıza danışabilirsiniz.");
            
            return suggestions.toString();
        } catch (Exception e) {
            log.error("Tax optimization suggestions error: {}", e.getMessage());
            return "Vergi optimizasyonu önerileri alınamadı.";
        }
    }

    @Override
    public TaxBreakdownDTO compareTaxRates(Long billId1, Long billId2) {
        log.info("Comparing tax rates for billId1: {} and billId2: {}", billId1, billId2);
        
        try {
            TaxBreakdownDTO taxBreakdown1 = analyzeTaxBreakdown(billId1);
            TaxBreakdownDTO taxBreakdown2 = analyzeTaxBreakdown(billId2);
            
            // Calculate differences
            BigDecimal amountDifference = taxBreakdown1.getTotalAmount().subtract(taxBreakdown2.getTotalAmount());
            BigDecimal taxDifference = taxBreakdown1.getTotalTax().subtract(taxBreakdown2.getTotalTax());
            BigDecimal rateDifference = taxBreakdown1.getEffectiveTaxRate().subtract(taxBreakdown2.getEffectiveTaxRate());
            
            // Build comparison result
            Map<String, BigDecimal> comparisonRates = new HashMap<>();
            comparisonRates.put("Fatura1_KDV", taxBreakdown1.getKdvAmount());
            comparisonRates.put("Fatura2_KDV", taxBreakdown2.getKdvAmount());
            comparisonRates.put("Fark_KDV", taxBreakdown1.getKdvAmount().subtract(taxBreakdown2.getKdvAmount()));
            comparisonRates.put("Fark_Oran", rateDifference);
            
            return TaxBreakdownDTO.builder()
                    .billId(billId1)
                    .totalAmount(amountDifference)
                    .totalTax(taxDifference)
                    .effectiveTaxRate(rateDifference)
                    .kdvAmount(taxBreakdown1.getKdvAmount().subtract(taxBreakdown2.getKdvAmount()))
                    .oivAmount(BigDecimal.ZERO)
                    .otherTaxes(taxBreakdown1.getOtherTaxes().subtract(taxBreakdown2.getOtherTaxes()))
                    .categoryTaxes(comparisonRates)
                    .build();
        } catch (Exception e) {
            log.error("Tax rate comparison error: {}", e.getMessage());
            Map<String, BigDecimal> taxRates = new HashMap<>();
            taxRates.put("KDV", new BigDecimal("0.18"));
            
            return TaxBreakdownDTO.builder()
                    .billId(billId1)
                    .totalAmount(BigDecimal.ZERO)
                    .totalTax(BigDecimal.ZERO)
                    .effectiveTaxRate(new BigDecimal("0.18"))
                    .kdvAmount(BigDecimal.ZERO)
                    .oivAmount(BigDecimal.ZERO)
                    .otherTaxes(BigDecimal.ZERO)
                    .categoryTaxes(taxRates)
                    .build();
        }
    }
}
