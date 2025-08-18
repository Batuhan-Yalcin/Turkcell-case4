package com.turkcellcase4.billing.controller;

import com.turkcellcase4.billing.dto.*;
import com.turkcellcase4.billing.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bonus")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BonusController {

    private final LLMExplanationService llmExplanationService;
    private final CohortService cohortService;
    private final TaxAnalysisService taxAnalysisService;
    private final AutofixService autofixService;

    // ===== LLM AÇIKLAMALARI =====
    
    /**
     * Anomali için AI destekli açıklama üretir
     */
    @PostMapping("/llm/anomaly")
    public ResponseEntity<String> getAnomalyExplanation(
            @RequestParam Long anomalyId,
            @RequestParam String userContext) {
        log.info("POST /bonus/llm/anomaly - Getting AI explanation for anomaly: {}", anomalyId);
        
        try {
            // Mock AnomalyDTO - gerçek uygulamada repository'den alınır
            AnomalyDTO anomaly = AnomalyDTO.builder()
                    .type(com.turkcellcase4.common.enums.AnomalyType.SPIKE)
                    .category("DATA")
                    .subtype("data_overage")
                    .delta(new java.math.BigDecimal("25.00"))
                    .percentageChange(new java.math.BigDecimal("180"))
                    .reason("Data aşımı nedeniyle ücret artışı")
                    .suggestedAction("Daha büyük plana geçin")
                    .build();
            
            String explanation = llmExplanationService.generateAnomalyExplanation(anomaly, userContext);
            return ResponseEntity.ok(explanation);
        } catch (Exception e) {
            log.error("Anomaly explanation error: {}", e.getMessage());
            return ResponseEntity.ok("AI açıklaması üretilemedi: " + e.getMessage());
        }
    }

    /**
     * Kohort analizi için AI destekli açıklama üretir
     */
    @PostMapping("/llm/cohort")
    public ResponseEntity<String> getCohortExplanation(
            @RequestParam Long userId,
            @RequestParam String period) {
        log.info("POST /bonus/llm/cohort - Getting AI explanation for cohort analysis: {}", userId);
        
        try {
            CohortAnalysisDTO cohortAnalysis = cohortService.analyzeUserCohort(userId, period);
            Double userAverage = cohortAnalysis.getUserAverage().doubleValue();
            Double cohortAverage = cohortAnalysis.getCohortAverage().doubleValue();
            
            String explanation = llmExplanationService.generateCohortAnalysis(userId, period, userAverage, cohortAverage);
            return ResponseEntity.ok(explanation);
        } catch (Exception e) {
            log.error("Cohort explanation error: {}", e.getMessage());
            return ResponseEntity.ok("AI açıklaması üretilemedi: " + e.getMessage());
        }
    }

    /**
     * Vergi analizi için AI destekli açıklama üretir
     */
    @PostMapping("/llm/tax")
    public ResponseEntity<String> getTaxExplanation(
            @RequestParam Long billId) {
        log.info("POST /bonus/llm/tax - Getting AI explanation for tax analysis: {}", billId);
        
        try {
            TaxBreakdownDTO taxBreakdown = taxAnalysisService.analyzeTaxBreakdown(billId);
            Double totalTax = taxBreakdown.getTotalTax().doubleValue();
            Double effectiveTaxRate = taxBreakdown.getEffectiveTaxRate().doubleValue();
            
            String explanation = llmExplanationService.generateTaxBreakdownAnalysis(billId, totalTax, effectiveTaxRate);
            return ResponseEntity.ok(explanation);
        } catch (Exception e) {
            log.error("Tax explanation error: {}", e.getMessage());
            return ResponseEntity.ok("AI açıklaması üretilemedi: " + e.getMessage());
        }
    }

    // ===== KOHORT KİYASI =====
    
    /**
     * Kullanıcının kohort analizini yapar
     */
    @GetMapping("/cohort/{userId}")
    public ResponseEntity<CohortAnalysisDTO> analyzeUserCohort(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /bonus/cohort/{}?period={} - Analyzing user cohort", userId, period);
        
        try {
            CohortAnalysisDTO cohortAnalysis = cohortService.analyzeUserCohort(userId, period);
            return ResponseEntity.ok(cohortAnalysis);
        } catch (Exception e) {
            log.error("Cohort analysis error: {}", e.getMessage());
            return ResponseEntity.ok(CohortAnalysisDTO.builder()
                    .userId(userId)
                    .period(period)
                    .userAverage(java.math.BigDecimal.ZERO)
                    .cohortAverage(java.math.BigDecimal.ZERO)
                    .performanceRating("ERROR")
                    .build());
        }
    }

    /**
     * Benzer kullanıcıları bulur
     */
    @GetMapping("/cohort/{userId}/similar")
    public ResponseEntity<CohortAnalysisDTO> findSimilarUsers(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /bonus/cohort/{}/similar?period={} - Finding similar users", userId, period);
        
        try {
            CohortAnalysisDTO similarUsers = cohortService.findSimilarUsers(userId, period);
            return ResponseEntity.ok(similarUsers);
        } catch (Exception e) {
            log.error("Similar users error: {}", e.getMessage());
            return ResponseEntity.ok(CohortAnalysisDTO.builder()
                    .userId(userId)
                    .period(period)
                    .userAverage(java.math.BigDecimal.ZERO)
                    .cohortAverage(java.math.BigDecimal.ZERO)
                    .performanceRating("ERROR")
                    .build());
        }
    }

    // ===== VERGİ AYRŞTIRMASI =====
    
    /**
     * Fatura için vergi ayrıştırması yapar
     */
    @GetMapping("/tax/{billId}")
    public ResponseEntity<TaxBreakdownDTO> analyzeTaxBreakdown(
            @PathVariable Long billId) {
        log.info("GET /bonus/tax/{} - Analyzing tax breakdown", billId);
        
        try {
            TaxBreakdownDTO taxBreakdown = taxAnalysisService.analyzeTaxBreakdown(billId);
            return ResponseEntity.ok(taxBreakdown);
        } catch (Exception e) {
            log.error("Tax breakdown error: {}", e.getMessage());
            return ResponseEntity.ok(TaxBreakdownDTO.builder()
                    .billId(billId)
                    .totalAmount(java.math.BigDecimal.ZERO)
                    .totalTax(java.math.BigDecimal.ZERO)
                    .effectiveTaxRate(java.math.BigDecimal.ZERO)
                    .build());
        }
    }

    /**
     * Kullanıcının vergi trendini analiz eder
     */
    @GetMapping("/tax/{userId}/trend")
    public ResponseEntity<TaxBreakdownDTO> analyzeUserTaxTrend(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int months) {
        log.info("GET /bonus/tax/{}/trend?months={} - Analyzing user tax trend", userId, months);
        
        try {
            TaxBreakdownDTO taxTrend = taxAnalysisService.analyzeUserTaxTrend(userId, months);
            return ResponseEntity.ok(taxTrend);
        } catch (Exception e) {
            log.error("Tax trend error: {}", e.getMessage());
            return ResponseEntity.ok(TaxBreakdownDTO.builder()
                    .userId(userId)
                    .totalAmount(java.math.BigDecimal.ZERO)
                    .totalTax(java.math.BigDecimal.ZERO)
                    .effectiveTaxRate(java.math.BigDecimal.ZERO)
                    .build());
        }
    }

    // ===== AUTOFIX ÖNERİLERİ =====
    
    /**
     * En iyi autofix önerisini üretir
     */
    @GetMapping("/autofix/{userId}/best")
    public ResponseEntity<AutofixRecommendationDTO> getBestAutofix(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /bonus/autofix/{}/best?period={} - Getting best autofix", userId, period);
        return ResponseEntity.ok(autofixService.generateBestAutofix(userId, period));
    }

    /**
     * Tüm autofix senaryolarını listeler
     */
    @GetMapping("/autofix/{userId}/scenarios")
    public ResponseEntity<List<AutofixRecommendationDTO>> getAllAutofixScenarios(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /bonus/autofix/{}/scenarios?period={} - Getting all autofix scenarios", userId, period);
        
        try {
            List<AutofixRecommendationDTO> scenarios = autofixService.getAllAutofixScenarios(userId, period);
            return ResponseEntity.ok(scenarios);
        } catch (Exception e) {
            log.error("Autofix scenarios error: {}", e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Autofix senaryosunu uygular
     */
    @PostMapping("/autofix/{userId}/apply")
    public ResponseEntity<String> applyAutofix(
            @PathVariable Long userId,
            @RequestParam String autofixId) {
        log.info("POST /bonus/autofix/{}/apply?autofixId={} - Applying autofix", userId, autofixId);
        
        try {
            String result = autofixService.applyAutofix(userId, autofixId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Autofix apply error: {}", e.getMessage());
            return ResponseEntity.ok("Autofix uygulanamadı: " + e.getMessage());
        }
    }

    // ===== GENEL BONUS ANALİZİ =====
    
    /**
     * Kullanıcı için tüm bonus analizleri
     */
    @GetMapping("/{userId}/analysis")
    public ResponseEntity<Map<String, Object>> getCompleteBonusAnalysis(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /bonus/{}/analysis?period={} - Getting complete bonus analysis", userId, period);
        
        try {
            // Tüm bonus servisleri entegre et
            CohortAnalysisDTO cohortAnalysis = cohortService.analyzeUserCohort(userId, period);
            TaxBreakdownDTO taxBreakdown = taxAnalysisService.analyzeUserTaxTrend(userId, 3);
            AutofixRecommendationDTO bestAutofix = autofixService.generateBestAutofix(userId, period);
            
            Map<String, Object> analysis = Map.of(
                "userId", userId,
                "period", period,
                "cohortAnalysis", cohortAnalysis,
                "taxAnalysis", taxBreakdown,
                "bestAutofix", bestAutofix,
                "message", "Tüm bonus analizleri başarıyla üretildi"
            );
            
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("Complete bonus analysis error: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "message", "Bonus analizleri üretilemedi: " + e.getMessage(),
                "userId", userId,
                "period", period
            ));
        }
    }
}
