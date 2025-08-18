package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.dto.AutofixRecommendationDTO;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.repository.BillItemRepository;
import com.turkcellcase4.billing.service.AutofixService;
import com.turkcellcase4.catalog.model.Plan;
import com.turkcellcase4.catalog.model.AddOnPack;
import com.turkcellcase4.catalog.repository.PlanRepository;
import com.turkcellcase4.catalog.repository.AddOnPackRepository;
import com.turkcellcase4.common.enums.ItemCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutofixServiceImpl implements AutofixService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PlanRepository planRepository;
    private final AddOnPackRepository addOnPackRepository;

    @Override
    public AutofixRecommendationDTO generateBestAutofix(Long userId, String period) {
        log.info("Getting best autofix for userId: {} and period: {}", userId, period);
        
        try {
            // Parse period
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate periodDate = LocalDate.parse(period + "-01", formatter);
            int year = periodDate.getYear();
            int month = periodDate.getMonthValue();
            
            // Get current bill
            Bill currentBill = billRepository.findByUserIdAndPeriod(userId, year, month)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));
            
            // Get all available plans and add-ons
            List<Plan> availablePlans = planRepository.findAll();
            List<AddOnPack> availableAddOns = addOnPackRepository.findAll();
            
            // Calculate current cost
            BigDecimal currentCost = currentBill.getTotalAmount();
            
            // Generate autofix scenarios
            List<AutofixRecommendationDTO> scenarios = generateAutofixScenarios(userId, period, currentBill, availablePlans, availableAddOns);
            
            // Find the best scenario (highest savings)
            AutofixRecommendationDTO bestScenario = scenarios.stream()
                    .max(Comparator.comparing(AutofixRecommendationDTO::getPotentialSavings))
                    .orElse(scenarios.get(0));
            
            return bestScenario;
        } catch (Exception e) {
            log.error("Best autofix generation error: {}", e.getMessage());
            return AutofixRecommendationDTO.builder()
                    .userId(userId)
                    .period(period)
                    .scenarioName("Hata")
                    .description("Autofix önerisi oluşturulamadı")
                    .category("ERROR")
                    .currentCost(BigDecimal.ZERO)
                    .newCost(BigDecimal.ZERO)
                    .potentialSavings(BigDecimal.ZERO)
                    .priority(0)
                    .riskLevel("HIGH")
                    .implementationDifficulty("HARD")
                    .isValid(false)
                    .status("ERROR")
                    .build();
        }
    }

    @Override
    public List<AutofixRecommendationDTO> getAllAutofixScenarios(Long userId, String period) {
        log.info("Getting all autofix scenarios for userId: {} and period: {}", userId, period);
        
        try {
            // Parse period
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate periodDate = LocalDate.parse(period + "-01", formatter);
            int year = periodDate.getYear();
            int month = periodDate.getMonthValue();
            
            // Get current bill
            Bill currentBill = billRepository.findByUserIdAndPeriod(userId, year, month)
                    .orElseThrow(() -> new RuntimeException("Bill not found"));
            
            // Get all available plans and add-ons
            List<Plan> availablePlans = planRepository.findAll();
            List<AddOnPack> availableAddOns = addOnPackRepository.findAll();
            
            // Generate all autofix scenarios
            return generateAutofixScenarios(userId, period, currentBill, availablePlans, availableAddOns);
        } catch (Exception e) {
            log.error("All autofix scenarios error: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public String applyAutofix(Long userId, String autofixId) {
        log.info("Applying autofix for userId: {} and autofixId: {}", userId, autofixId);
        
        try {
            // Validate autofix
            if (!validateAutofix(userId, autofixId)) {
                return "Autofix doğrulanamadı: " + autofixId;
            }
            
            // Apply the autofix (mock implementation)
            // In real implementation, this would call checkout service
            return "Autofix başarıyla uygulandı: " + autofixId + ". Değişiklikler bir sonraki fatura döneminde aktif olacak.";
        } catch (Exception e) {
            log.error("Autofix application error: {}", e.getMessage());
            return "Autofix uygulanırken hata oluştu: " + e.getMessage();
        }
    }

    @Override
    public List<AutofixRecommendationDTO> getPrioritizedAutofixes(Long userId, String period) {
        log.info("Getting prioritized autofixes for userId: {} and period: {}", userId, period);
        
        try {
            List<AutofixRecommendationDTO> allScenarios = getAllAutofixScenarios(userId, period);
            
            // Sort by priority (1 = highest) and then by potential savings
            return allScenarios.stream()
                    .sorted(Comparator.comparing(AutofixRecommendationDTO::getPriority)
                            .thenComparing(AutofixRecommendationDTO::getPotentialSavings, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Prioritized autofixes error: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean validateAutofix(Long userId, String autofixId) {
        log.info("Validating autofix for userId: {} and autofixId: {}", userId, autofixId);
        
        try {
            // Check if autofix exists and belongs to user
            // In real implementation, this would validate against database
            if (autofixId == null || autofixId.isEmpty()) {
                return false;
            }
            
            // Check if user exists
            // This is a simple validation - in real implementation, check against database
            return userId != null && userId > 0;
        } catch (Exception e) {
            log.error("Autofix validation error: {}", e.getMessage());
            return false;
        }
    }
    
    private List<AutofixRecommendationDTO> generateAutofixScenarios(Long userId, String period, Bill currentBill, 
                                                                   List<Plan> availablePlans, List<AddOnPack> availableAddOns) {
        List<AutofixRecommendationDTO> scenarios = new ArrayList<>();
        
        try {
            BigDecimal currentCost = currentBill.getTotalAmount();
            List<BillItem> items = billItemRepository.findByBill_BillId(currentBill.getBillId());
            
            // Scenario 1: Plan Change
            if (availablePlans.size() > 1) {
                Plan cheapestPlan = availablePlans.stream()
                        .min(Comparator.comparing(Plan::getMonthlyPrice))
                        .orElse(availablePlans.get(0));
                
                BigDecimal newCost = cheapestPlan.getMonthlyPrice();
                BigDecimal savings = currentCost.subtract(newCost);
                
                if (savings.compareTo(BigDecimal.ZERO) > 0) {
                    scenarios.add(AutofixRecommendationDTO.builder()
                            .userId(userId)
                            .period(period)
                            .scenarioName("Plan Değişikliği")
                            .description("En ucuz plana geçerek tasarruf edin: " + cheapestPlan.getPlanName())
                            .category("PLAN_CHANGE")
                            .currentCost(currentCost)
                            .newCost(newCost)
                            .potentialSavings(savings)
                            .priority(1)
                            .riskLevel("LOW")
                            .implementationDifficulty("EASY")
                            .isValid(true)
                            .status("PENDING")
                            .build());
                }
            }
            
            // Scenario 2: VAS Cancellation
            BigDecimal vasTotal = items.stream()
                    .filter(item -> ItemCategory.VAS.equals(item.getCategory()) && !"plan_fee".equals(item.getSubtype()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (vasTotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newCost = currentCost.subtract(vasTotal);
                scenarios.add(AutofixRecommendationDTO.builder()
                        .userId(userId)
                        .period(period)
                        .scenarioName("VAS İptali")
                        .description("Kullanmadığınız VAS servislerini kapatarak tasarruf edin")
                        .category("VAS_CANCEL")
                        .currentCost(currentCost)
                        .newCost(newCost)
                        .potentialSavings(vasTotal)
                        .priority(2)
                        .riskLevel("LOW")
                        .implementationDifficulty("EASY")
                        .isValid(true)
                        .status("PENDING")
                        .build());
            }
            
            // Scenario 3: Premium SMS Block
            BigDecimal premiumSMSTotal = items.stream()
                    .filter(item -> ItemCategory.PREMIUM_SMS.equals(item.getCategory()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (premiumSMSTotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newCost = currentCost.subtract(premiumSMSTotal);
                scenarios.add(AutofixRecommendationDTO.builder()
                        .userId(userId)
                        .period(period)
                        .scenarioName("Premium SMS Engelleme")
                        .description("Premium SMS'i engelleyerek tasarruf edin")
                        .category("PREMIUM_SMS_BLOCK")
                        .currentCost(currentCost)
                        .newCost(newCost)
                        .potentialSavings(premiumSMSTotal)
                        .priority(3)
                        .riskLevel("LOW")
                        .implementationDifficulty("EASY")
                        .isValid(true)
                        .status("PENDING")
                        .build());
            }
            
            // Scenario 4: Add-on Optimization
            if (!availableAddOns.isEmpty()) {
                AddOnPack dataAddon = availableAddOns.stream()
                        .filter(addon -> "data".equals(addon.getType()))
                        .findFirst()
                        .orElse(availableAddOns.get(0));
                
                BigDecimal addonCost = dataAddon.getPrice();
                BigDecimal dataOverageTotal = items.stream()
                        .filter(item -> ItemCategory.DATA.equals(item.getCategory()) && "data_overage".equals(item.getSubtype()))
                        .map(BillItem::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (dataOverageTotal.compareTo(addonCost) > 0) {
                    BigDecimal savings = dataOverageTotal.subtract(addonCost);
                    scenarios.add(AutofixRecommendationDTO.builder()
                            .userId(userId)
                            .period(period)
                            .scenarioName("Ek Paket Ekleme")
                            .description("Data paketi ekleyerek aşım ücretlerinden tasarruf edin: " + dataAddon.getName())
                            .category("ADDON_ADD")
                            .currentCost(currentCost)
                            .newCost(currentCost.subtract(savings))
                            .potentialSavings(savings)
                            .priority(4)
                            .riskLevel("MEDIUM")
                            .implementationDifficulty("MEDIUM")
                            .isValid(true)
                            .status("PENDING")
                            .build());
                }
            }
            
            // If no scenarios found, add a default one
            if (scenarios.isEmpty()) {
                scenarios.add(AutofixRecommendationDTO.builder()
                        .userId(userId)
                        .period(period)
                        .scenarioName("Tasarruf Yok")
                        .description("Bu dönemde tasarruf fırsatı bulunmuyor")
                        .category("NO_SAVINGS")
                        .currentCost(currentCost)
                        .newCost(currentCost)
                        .potentialSavings(BigDecimal.ZERO)
                        .priority(5)
                        .riskLevel("NONE")
                        .implementationDifficulty("NONE")
                        .isValid(true)
                        .status("NO_ACTION")
                        .build());
            }
            
        } catch (Exception e) {
            log.error("Autofix scenarios generation error: {}", e.getMessage());
        }
        
        return scenarios;
    }
}
