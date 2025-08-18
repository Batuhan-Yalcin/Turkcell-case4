package com.turkcellcase4.simulation.service.impl;

import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.repository.BillItemRepository;
import com.turkcellcase4.billing.service.BillService;
import com.turkcellcase4.catalog.model.Plan;
import com.turkcellcase4.catalog.model.AddOnPack;
import com.turkcellcase4.catalog.repository.PlanRepository;
import com.turkcellcase4.catalog.repository.AddOnPackRepository;
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

    @Override
    public SimulationResponseDTO simulateScenario(SimulationRequestDTO request) {
        log.info("Simulating scenario for user: {} and period: {}", request.getUserId(), request.getPeriod());
        
        // Validate user and get current bill
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Bill currentBill = getCurrentBill(request.getUserId(), request.getPeriod());
        if (currentBill == null) {
            throw new RuntimeException("No bill found for the specified period");
        }
        
        BigDecimal currentTotal = currentBill.getTotalAmount();
        SimulationScenarioDTO scenario = request.getScenario();
        
        // Calculate new total based on scenario
        BigDecimal newTotal = calculateNewTotal(currentBill, scenario, request.getUserId(), request.getPeriod());
        BigDecimal savings = currentTotal.subtract(newTotal);
        
        // Generate detailed breakdown
        String details = generateScenarioDetails(scenario, newTotal, savings);
        
        return SimulationResponseDTO.builder()
                .newTotal(newTotal)
                .saving(savings)
                .details(details)
                .scenario(scenario)
                .build();
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
        
        // This would normally query usage_daily table
        // For now, return mock data based on bill items
        Map<String, BigDecimal> usage = new HashMap<>();
        usage.put("data_gb", new BigDecimal("8.5")); // Mock data usage
        usage.put("voice_min", new BigDecimal("180"));
        usage.put("sms_count", new BigDecimal("45"));
        usage.put("roaming_mb", new BigDecimal("0"));
        
        return usage;
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

    private String generateScenarioDetails(SimulationScenarioDTO scenario, BigDecimal newTotal, BigDecimal savings) {
        StringBuilder details = new StringBuilder();
        
        if (scenario.getPlanId() != null) {
            details.append("Plan changed to ID: ").append(scenario.getPlanId()).append(". ");
        }
        
        if (scenario.getAddons() != null && !scenario.getAddons().isEmpty()) {
            details.append("Add-ons added: ").append(scenario.getAddons().size()).append(". ");
        }
        
        if (Boolean.TRUE.equals(scenario.getDisableVas())) {
            details.append("VAS services disabled. ");
        }
        
        if (Boolean.TRUE.equals(scenario.getBlockPremiumSms())) {
            details.append("Premium SMS blocked. ");
        }
        
        details.append("New total: ").append(newTotal).append(" TL. ");
        
        if (savings.compareTo(BigDecimal.ZERO) > 0) {
            details.append("Potential savings: ").append(savings).append(" TL.");
        } else if (savings.compareTo(BigDecimal.ZERO) < 0) {
            details.append("Additional cost: ").append(savings.abs()).append(" TL.");
        } else {
            details.append("No cost change.");
        }
        
        return details.toString();
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
