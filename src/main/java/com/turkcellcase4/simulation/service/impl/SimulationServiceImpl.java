package com.turkcellcase4.simulation.service.impl;

import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.billing.model.UsageDaily;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.repository.BillItemRepository;
import com.turkcellcase4.billing.service.BillService;
import com.turkcellcase4.catalog.model.Plan;
import com.turkcellcase4.catalog.model.AddOnPack;
import com.turkcellcase4.catalog.repository.PlanRepository;
import com.turkcellcase4.catalog.repository.AddOnPackRepository;
import com.turkcellcase4.billing.repository.UsageDailyRepository;
import com.turkcellcase4.common.enums.ItemCategory;
import com.turkcellcase4.simulation.dto.*;
import com.turkcellcase4.simulation.service.SimulationService;
import com.turkcellcase4.user.model.User;
import com.turkcellcase4.user.repository.UserRepository;
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
import com.turkcellcase4.common.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimulationServiceImpl implements SimulationService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PlanRepository planRepository;
    private final AddOnPackRepository addOnPackRepository;
    private final UserRepository userRepository;
    private final BillService billService;
    private final UsageDailyRepository usageDailyRepository;

    @Override
    public SimulationResponseDTO simulateScenario(SimulationRequestDTO request) {
        log.info("Simulating scenario for user: {} and period: {}", request.getUserId(), request.getPeriod());
        
        try {
            // Validate user and get current bill
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + request.getUserId()));
            
            Bill currentBill = getCurrentBill(request.getUserId(), request.getPeriod());
            if (currentBill == null) {
                throw new ResourceNotFoundException("Belirtilen dönem için fatura bulunamadı");
            }
            
            BigDecimal currentTotal = currentBill.getTotalAmount();
            SimulationScenarioDTO scenario = request.getScenario();
            
            // Calculate new total based on scenario
            BigDecimal newTotal = calculateNewTotal(currentBill, scenario, request.getUserId(), request.getPeriod());
            BigDecimal savings = currentTotal.subtract(newTotal);
            
            // Generate detailed breakdown
            String details = generateScenarioDetails(scenario, newTotal, savings);
            
            // Generate recommendations
            List<String> recommendations = generateRecommendations(scenario, savings, currentTotal);
            
            return SimulationResponseDTO.builder()
                    .newTotal(newTotal)
                    .saving(savings)
                    .details(details)
                    .scenario(scenario)
                    .recommendations(recommendations)
                    .currentTotal(currentTotal)
                    .build();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new BusinessLogicException("Senaryo simülasyonu hatası: " + e.getMessage());
        }
    }

    @Override
    public SimulationResponseDTO getScenarios(Long userId) {
        log.info("Getting scenarios for user: {}", userId);
        
        // Get current plan and available alternatives
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Plan> availablePlans = planRepository.findAll();
        List<AddOnPack> availableAddOns = addOnPackRepository.findAll();
        
        // Generate top 3 scenarios
        List<SimulationScenarioDTO> topScenarios = generateTopScenarios(user, availablePlans, availableAddOns);
        
        return SimulationResponseDTO.builder()
                .scenarios(topScenarios)
                .build();
    }

    @Override
    public SimulationResponseDTO compareScenarios(SimulationRequestDTO request) {
        log.info("Comparing scenarios for user: {}", request.getUserId());
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Bill currentBill = getCurrentBill(request.getUserId(), request.getPeriod());
        if (currentBill == null) {
            throw new RuntimeException("No bill found for the specified period");
        }
        
        BigDecimal currentTotal = currentBill.getTotalAmount();
        
        // Generate multiple scenarios for comparison
        List<SimulationScenarioDTO> scenarios = generateComparisonScenarios(user);
        List<ScenarioComparisonDTO> comparisons = new ArrayList<>();
        
        for (SimulationScenarioDTO scenario : scenarios) {
            BigDecimal newTotal = calculateNewTotal(currentBill, scenario, request.getUserId(), request.getPeriod());
            BigDecimal savings = currentTotal.subtract(newTotal);
            
            comparisons.add(ScenarioComparisonDTO.builder()
                    .scenario(scenario)
                    .newTotal(newTotal)
                    .savings(savings)
                    .build());
        }
        
        // Sort by savings (descending)
        comparisons.sort((a, b) -> b.getSavings().compareTo(a.getSavings()));
        
        return SimulationResponseDTO.builder()
                .comparisons(comparisons)
                .build();
    }

    @Override
    public SimulationResponseDTO getWhatIfAnalysis(Long userId, String period) {
        log.info("Getting what-if analysis for user: {} and period: {}", userId, period);
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + userId));
            
            Bill currentBill = getCurrentBill(userId, period);
            if (currentBill == null) {
                throw new ResourceNotFoundException("Belirtilen dönem için fatura bulunamadı");
            }
            
            BigDecimal currentTotal = currentBill.getTotalAmount();
            
            // Generate comprehensive what-if scenarios
            List<SimulationScenarioDTO> whatIfScenarios = generateWhatIfScenarios(user, currentBill);
            List<ScenarioComparisonDTO> whatIfComparisons = new ArrayList<>();
            
            for (SimulationScenarioDTO scenario : whatIfScenarios) {
                BigDecimal newTotal = calculateNewTotal(currentBill, scenario, userId, period);
                BigDecimal savings = currentTotal.subtract(newTotal);
                
                whatIfComparisons.add(ScenarioComparisonDTO.builder()
                        .scenario(scenario)
                        .newTotal(newTotal)
                        .savings(savings)
                        .build());
            }
            
            // Sort by savings (descending) and take top 5
            whatIfComparisons.sort((a, b) -> b.getSavings().compareTo(a.getSavings()));
            List<ScenarioComparisonDTO> topScenarios = whatIfComparisons.stream()
                    .limit(5)
                    .collect(Collectors.toList());
            
            // Generate summary insights
            String summary = generateWhatIfSummary(currentTotal, topScenarios);
            
            return SimulationResponseDTO.builder()
                    .comparisons(topScenarios)
                    .summary(summary)
                    .currentTotal(currentTotal)
                    .build();
                    
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                throw e;
            }
            throw new BusinessLogicException("What-if analizi hatası: " + e.getMessage());
        }
    }

    private BigDecimal calculateNewTotal(Bill currentBill, SimulationScenarioDTO scenario, Long userId, String period) {
        BigDecimal newTotal = BigDecimal.ZERO;
        
        // Get current usage data
        Map<String, BigDecimal> usageData = getUsageData(userId, period);
        
        // Calculate plan cost
        if (scenario.getPlanId() != null) {
            Plan newPlan = planRepository.findById(scenario.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Plan not found"));
            
            newTotal = newTotal.add(newPlan.getMonthlyPrice());
            
            // Calculate overage costs
            newTotal = newTotal.add(calculateDataOverage(usageData.get("data_gb"), newPlan.getQuotaGb(), newPlan.getOverageGb()));
            newTotal = newTotal.add(calculateVoiceOverage(usageData.get("voice_min"), newPlan.getQuotaMin(), newPlan.getOverageMin()));
            newTotal = newTotal.add(calculateSMSOverage(usageData.get("sms_count"), newPlan.getQuotaSms(), newPlan.getOverageSms()));
        } else {
            // Keep current plan cost
            newTotal = newTotal.add(getCurrentPlanCost(currentBill));
        }
        
        // Add add-on costs
        if (scenario.getAddons() != null && !scenario.getAddons().isEmpty()) {
            for (Long addonId : scenario.getAddons()) {
                AddOnPack addon = addOnPackRepository.findById(addonId)
                        .orElseThrow(() -> new RuntimeException("Add-on not found"));
                newTotal = newTotal.add(addon.getPrice());
            }
        }
        
        // Calculate VAS and Premium SMS costs (if not disabled)
        if (!Boolean.TRUE.equals(scenario.getDisableVas())) {
            newTotal = newTotal.add(getVASCost(currentBill));
        }
        
        if (!Boolean.TRUE.equals(scenario.getBlockPremiumSms())) {
            newTotal = newTotal.add(getPremiumSMSCost(currentBill));
        }
        
        // Add taxes and other costs
        newTotal = newTotal.add(getTaxesAndOtherCosts(currentBill));
        
        return newTotal.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDataOverage(BigDecimal usedGB, Double quotaGB, BigDecimal overageRate) {
        if (usedGB == null || quotaGB == null || overageRate == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal quotaGBDecimal = BigDecimal.valueOf(quotaGB);
        BigDecimal overageGB = usedGB.subtract(quotaGBDecimal);
        if (overageGB.compareTo(BigDecimal.ZERO) > 0) {
            return overageGB.multiply(overageRate);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateVoiceOverage(BigDecimal usedMinutes, Integer quotaMinutes, BigDecimal overageRate) {
        if (usedMinutes == null || quotaMinutes == null || overageRate == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal quotaMinutesDecimal = BigDecimal.valueOf(quotaMinutes);
        BigDecimal overageMinutes = usedMinutes.subtract(quotaMinutesDecimal);
        if (overageMinutes.compareTo(BigDecimal.ZERO) > 0) {
            return overageMinutes.multiply(overageRate);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateSMSOverage(BigDecimal usedSMS, Integer quotaSMS, BigDecimal overageRate) {
        if (usedSMS == null || quotaSMS == null || overageRate == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal quotaSMSDecimal = BigDecimal.valueOf(quotaSMS);
        BigDecimal overageSMS = usedSMS.subtract(quotaSMSDecimal);
        if (overageSMS.compareTo(BigDecimal.ZERO) > 0) {
            return overageSMS.multiply(overageRate);
        }
        return BigDecimal.ZERO;
    }

    private Map<String, BigDecimal> getUsageData(Long userId, String period) {
        // Parse period (YYYY-MM format)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate periodStart = LocalDate.parse(period + "-01", formatter);
        LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
        
        // Get real usage data from usage_daily table
        try {
            List<UsageDaily> usageData = usageDailyRepository.findByUser_UserIdAndDateBetween(userId, periodStart, periodEnd);
            
            Map<String, BigDecimal> usage = new HashMap<>();
            
            if (!usageData.isEmpty()) {
                // Calculate total data usage in GB
                double totalDataMB = usageData.stream()
                    .mapToDouble(ud -> ud.getMbUsed() != null ? ud.getMbUsed() : 0.0)
                    .sum();
                usage.put("data_gb", BigDecimal.valueOf(totalDataMB / 1024.0));
                
                // Calculate total voice usage in minutes
                int totalVoiceMinutes = usageData.stream()
                    .mapToInt(ud -> ud.getMinutesUsed() != null ? ud.getMinutesUsed() : 0)
                    .sum();
                usage.put("voice_min", BigDecimal.valueOf(totalVoiceMinutes));
                
                // Calculate total SMS usage
                int totalSMSCount = usageData.stream()
                    .mapToInt(ud -> ud.getSmsUsed() != null ? ud.getSmsUsed() : 0)
                    .sum();
                usage.put("sms_count", BigDecimal.valueOf(totalSMSCount));
                
                // Calculate total roaming usage in MB
                double totalRoamingMB = usageData.stream()
                    .mapToDouble(ud -> ud.getRoamingMb() != null ? ud.getRoamingMb() : 0.0)
                    .sum();
                usage.put("roaming_mb", BigDecimal.valueOf(totalRoamingMB));
            } else {
                // Fallback to bill items if no usage data
                usage.put("data_gb", BigDecimal.ZERO);
                usage.put("voice_min", BigDecimal.ZERO);
                usage.put("sms_count", BigDecimal.ZERO);
                usage.put("roaming_mb", BigDecimal.ZERO);
            }
            
            return usage;
        } catch (Exception e) {
            log.warn("Usage data alınamadı, fallback kullanılıyor: {}", e.getMessage());
            
            // Fallback to bill items
            Map<String, BigDecimal> usage = new HashMap<>();
            usage.put("data_gb", BigDecimal.ZERO);
            usage.put("voice_min", BigDecimal.ZERO);
            usage.put("sms_count", BigDecimal.ZERO);
            usage.put("roaming_mb", BigDecimal.ZERO);
            
            return usage;
        }
    }

    private BigDecimal getCurrentPlanCost(Bill bill) {
        // Extract plan cost from bill items
        List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
        return items.stream()
                .filter(item -> ItemCategory.VAS.equals(item.getCategory()) && "plan_fee".equals(item.getSubtype()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getVASCost(Bill bill) {
        List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
        return items.stream()
                .filter(item -> ItemCategory.VAS.equals(item.getCategory()) && !"plan_fee".equals(item.getSubtype()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getPremiumSMSCost(Bill bill) {
        List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
        return items.stream()
                .filter(item -> ItemCategory.PREMIUM_SMS.equals(item.getCategory()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTaxesAndOtherCosts(Bill bill) {
        List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());
        return items.stream()
                .filter(item -> ItemCategory.TAX.equals(item.getCategory()) || ItemCategory.ONE_OFF.equals(item.getCategory()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<SimulationScenarioDTO> generateTopScenarios(User user, List<Plan> plans, List<AddOnPack> addOns) {
        List<SimulationScenarioDTO> scenarios = new ArrayList<>();
        
        // Scenario 1: Change to cheaper plan
        if (plans.size() > 1) {
            Plan cheapestPlan = plans.stream()
                    .min(Comparator.comparing(Plan::getMonthlyPrice))
                    .orElse(plans.get(0));
            
            scenarios.add(SimulationScenarioDTO.builder()
                    .planId(cheapestPlan.getPlanId())
                    .description("Switch to " + cheapestPlan.getPlanName())
                    .build());
        }
        
        // Scenario 2: Add data add-on
        if (!addOns.isEmpty()) {
            AddOnPack dataAddon = addOns.stream()
                    .filter(addon -> "data".equals(addon.getType()))
                    .findFirst()
                    .orElse(addOns.get(0));
            
            scenarios.add(SimulationScenarioDTO.builder()
                    .addons(Arrays.asList(dataAddon.getAddonId()))
                    .description("Add " + dataAddon.getName())
                    .build());
        }
        
        // Scenario 3: Disable VAS and Premium SMS
        scenarios.add(SimulationScenarioDTO.builder()
                .disableVas(true)
                .blockPremiumSms(true)
                .description("Disable VAS and Premium SMS")
                .build());
        
        return scenarios;
    }

    private List<SimulationScenarioDTO> generateComparisonScenarios(User user) {
        List<SimulationScenarioDTO> scenarios = new ArrayList<>();
        
        // Current plan
        scenarios.add(SimulationScenarioDTO.builder()
                .description("Keep current plan")
                .build());
        
        // Cheaper plan
        scenarios.add(SimulationScenarioDTO.builder()
                .planId(2L) // Mock cheaper plan ID
                .description("Switch to cheaper plan")
                .build());
        
        // Premium plan with more quota
        scenarios.add(SimulationScenarioDTO.builder()
                .planId(3L) // Mock premium plan ID
                .description("Upgrade to premium plan")
                .build());
        
        // Add data add-on
        scenarios.add(SimulationScenarioDTO.builder()
                .addons(Arrays.asList(1L)) // Mock add-on ID
                .description("Add data package")
                .build());
        
        // Disable VAS
        scenarios.add(SimulationScenarioDTO.builder()
                .disableVas(true)
                .description("Disable VAS services")
                .build());
        
        return scenarios;
    }

    private List<SimulationScenarioDTO> generateWhatIfScenarios(User user, Bill currentBill) {
        List<SimulationScenarioDTO> scenarios = new ArrayList<>();
        
        // Get available plans and add-ons
        List<Plan> availablePlans = planRepository.findAll();
        List<AddOnPack> availableAddOns = addOnPackRepository.findAll();
        
        // Scenario 1: Keep current plan (baseline)
        scenarios.add(SimulationScenarioDTO.builder()
                .description("Mevcut planı koru")
                .build());
        
        // Scenario 2: Switch to cheapest plan
        if (availablePlans.size() > 1) {
            Plan cheapestPlan = availablePlans.stream()
                    .min(Comparator.comparing(Plan::getMonthlyPrice))
                    .orElse(availablePlans.get(0));
            
            scenarios.add(SimulationScenarioDTO.builder()
                    .planId(cheapestPlan.getPlanId())
                    .description("En ucuz plana geç: " + cheapestPlan.getPlanName())
                    .build());
        }
        
        // Scenario 3: Switch to premium plan with more quota
        if (availablePlans.size() > 2) {
            Plan premiumPlan = availablePlans.stream()
                    .max(Comparator.comparing(Plan::getQuotaGb))
                    .orElse(availablePlans.get(0));
            
            scenarios.add(SimulationScenarioDTO.builder()
                    .planId(premiumPlan.getPlanId())
                    .description("Premium plana yükselt: " + premiumPlan.getPlanName())
                    .build());
        }
        
        // Scenario 4: Add data add-on
        if (!availableAddOns.isEmpty()) {
            AddOnPack dataAddon = availableAddOns.stream()
                    .filter(addon -> "data".equals(addon.getType()))
                    .findFirst()
                    .orElse(availableAddOns.get(0));
            
            scenarios.add(SimulationScenarioDTO.builder()
                    .addons(Arrays.asList(dataAddon.getAddonId()))
                    .description("Data paketi ekle: " + dataAddon.getName())
                    .build());
        }
        
        // Scenario 5: Add voice add-on
        if (availableAddOns.size() > 1) {
            AddOnPack voiceAddon = availableAddOns.stream()
                    .filter(addon -> "voice".equals(addon.getType()))
                    .findFirst()
                    .orElse(availableAddOns.get(0));
            
            scenarios.add(SimulationScenarioDTO.builder()
                    .addons(Arrays.asList(voiceAddon.getAddonId()))
                    .description("Ses paketi ekle: " + voiceAddon.getName())
                    .build());
        }
        
        // Scenario 6: Disable VAS services
        scenarios.add(SimulationScenarioDTO.builder()
                .disableVas(true)
                .description("VAS servislerini devre dışı bırak")
                .build());
        
        // Scenario 7: Block Premium SMS
        scenarios.add(SimulationScenarioDTO.builder()
                .blockPremiumSms(true)
                .description("Premium SMS'i engelle")
                .build());
        
        // Scenario 8: Disable VAS and Premium SMS
        scenarios.add(SimulationScenarioDTO.builder()
                .disableVas(true)
                .blockPremiumSms(true)
                .description("VAS ve Premium SMS'i devre dışı bırak")
                .build());
        
        // Scenario 9: Combine plan change with add-on
        if (availablePlans.size() > 1 && !availableAddOns.isEmpty()) {
            Plan midPlan = availablePlans.get(1); // Second plan
            AddOnPack dataAddon = availableAddOns.get(0);
            
            scenarios.add(SimulationScenarioDTO.builder()
                    .planId(midPlan.getPlanId())
                    .addons(Arrays.asList(dataAddon.getAddonId()))
                    .description("Orta plana geç + data paketi ekle")
                    .build());
        }
        
        // Scenario 10: Optimize for current usage
        scenarios.add(SimulationScenarioDTO.builder()
                .description("Mevcut kullanıma göre optimize et")
                .build());
        
        return scenarios;
    }

    private String generateScenarioDetails(SimulationScenarioDTO scenario, BigDecimal newTotal, BigDecimal savings) {
        StringBuilder details = new StringBuilder();
        
        if (scenario.getPlanId() != null) {
            details.append("Plan değişikliği: ID ").append(scenario.getPlanId()).append(". ");
        }
        
        if (scenario.getAddons() != null && !scenario.getAddons().isEmpty()) {
            details.append("Ek paketler eklendi: ").append(scenario.getAddons().size()).append(" adet. ");
        }
        
        if (Boolean.TRUE.equals(scenario.getDisableVas())) {
            details.append("VAS servisleri devre dışı. ");
        }
        
        if (Boolean.TRUE.equals(scenario.getBlockPremiumSms())) {
            details.append("Premium SMS engellendi. ");
        }
        
        details.append("Yeni toplam: ").append(newTotal).append(" TL. ");
        
        if (savings.compareTo(BigDecimal.ZERO) > 0) {
            details.append("Potansiyel tasarruf: ").append(savings).append(" TL.");
        } else if (savings.compareTo(BigDecimal.ZERO) < 0) {
            details.append("Ek maliyet: ").append(savings.abs()).append(" TL.");
        } else {
            details.append("Maliyet değişikliği yok.");
        }
        
        return details.toString();
    }

    private List<String> generateRecommendations(SimulationScenarioDTO scenario, BigDecimal savings, BigDecimal currentTotal) {
        List<String> recommendations = new ArrayList<>();
        
        if (savings.compareTo(BigDecimal.ZERO) > 0) {
            recommendations.add("Bu senaryo ile " + savings + " TL tasarruf edebilirsiniz.");
            
            if (savings.compareTo(currentTotal.multiply(new BigDecimal("0.2"))) > 0) {
                recommendations.add("Önemli tasarruf fırsatı! Toplam faturanızın %20'sinden fazla tasarruf.");
            }
        } else if (savings.compareTo(BigDecimal.ZERO) < 0) {
            recommendations.add("Bu senaryo ek maliyet getiriyor. Dikkatli değerlendirin.");
        }
        
        if (Boolean.TRUE.equals(scenario.getDisableVas())) {
            recommendations.add("VAS servislerini kapatarak gereksiz ücretlerden kaçınabilirsiniz.");
        }
        
        if (Boolean.TRUE.equals(scenario.getBlockPremiumSms())) {
            recommendations.add("Premium SMS engelleme ile beklenmedik ücretleri önleyebilirsiniz.");
        }
        
        if (scenario.getAddons() != null && !scenario.getAddons().isEmpty()) {
            recommendations.add("Ek paketler ile aşım ücretlerini azaltabilirsiniz.");
        }
        
        return recommendations;
    }

    private String generateWhatIfSummary(BigDecimal currentTotal, List<ScenarioComparisonDTO> topScenarios) {
        if (topScenarios.isEmpty()) {
            return "Senaryo analizi yapılamadı.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Mevcut fatura tutarınız: ").append(currentTotal).append(" TL. ");
        
        // Best scenario
        ScenarioComparisonDTO bestScenario = topScenarios.get(0);
        if (bestScenario.getSavings().compareTo(BigDecimal.ZERO) > 0) {
            summary.append("En iyi senaryo ile ").append(bestScenario.getSavings()).append(" TL tasarruf edebilirsiniz. ");
            summary.append(bestScenario.getScenario().getDescription()).append(" ");
        }
        
        // Average savings
        BigDecimal avgSavings = topScenarios.stream()
                .map(ScenarioComparisonDTO::getSavings)
                .filter(savings -> savings.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(topScenarios.size()), 2, RoundingMode.HALF_UP);
        
        if (avgSavings.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("Ortalama tasarruf potansiyeli: ").append(avgSavings).append(" TL.");
        }
        
        return summary.toString();
    }

    private Bill getCurrentBill(Long userId, String period) {
        // Parse period and find bill
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate periodStart = LocalDate.parse(period + "-01", formatter);
        LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
        
        return billRepository.findByUser_UserIdAndPeriodStartBetween(userId, periodStart, periodEnd)
                .stream()
                .findFirst()
                .orElse(null);
    }
}
