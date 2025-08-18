package com.turkcellcase4.billing.controller;

import com.turkcellcase4.billing.dto.UsageDTO;
import com.turkcellcase4.billing.dto.UsageSummaryDTO;
import com.turkcellcase4.billing.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/usage")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UsageController {

    private final UsageService usageService;

    /**
     * Kullanıcının belirli dönemdeki günlük kullanım verilerini getirir
     */
    @GetMapping("/{userId}/daily")
    public ResponseEntity<List<UsageDTO>> getDailyUsage(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /usage/{}/daily?period={} - Getting daily usage for user and period", userId, period);
        List<UsageDTO> usage = usageService.getDailyUsage(userId, period);
        return ResponseEntity.ok(usage);
    }

    /**
     * Kullanıcının belirli tarih aralığındaki günlük kullanım verilerini getirir
     */
    @GetMapping("/{userId}/daily/range")
    public ResponseEntity<List<UsageDTO>> getDailyUsageByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /usage/{}/daily/range?startDate={}&endDate={} - Getting daily usage in date range", userId, startDate, endDate);
        List<UsageDTO> usage = usageService.getDailyUsageByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(usage);
    }

    /**
     * Kullanıcının belirli dönemdeki kullanım özetini getirir
     */
    @GetMapping("/{userId}/summary")
    public ResponseEntity<UsageSummaryDTO> getUsageSummary(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /usage/{}/summary?period={} - Getting usage summary for user and period", userId, period);
        UsageSummaryDTO summary = usageService.getUsageSummary(userId, period);
        return ResponseEntity.ok(summary);
    }

    /**
     * Kullanıcının belirli tarih aralığındaki kullanım özetini getirir
     */
    @GetMapping("/{userId}/summary/range")
    public ResponseEntity<UsageSummaryDTO> getUsageSummaryByDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /usage/{}/summary/range?startDate={}&endDate={} - Getting usage summary in date range", userId, startDate, endDate);
        UsageSummaryDTO summary = usageService.getUsageSummaryByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    /**
     * Kullanıcının son N ay kullanım trendini analiz eder
     */
    @GetMapping("/{userId}/trend")
    public ResponseEntity<UsageSummaryDTO> analyzeUsageTrend(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int months) {
        log.info("GET /usage/{}/trend?months={} - Analyzing usage trend for user", userId, months);
        UsageSummaryDTO trend = usageService.analyzeUsageTrend(userId, months);
        return ResponseEntity.ok(trend);
    }

    /**
     * Kullanıcının data kullanım analizini getirir
     */
    @GetMapping("/{userId}/analysis/data")
    public ResponseEntity<String> getDataUsageAnalysis(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /usage/{}/analysis/data?period={} - Getting data usage analysis", userId, period);
        String analysis = usageService.getDataUsageAnalysis(userId, period);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Kullanıcının ses kullanım analizini getirir
     */
    @GetMapping("/{userId}/analysis/voice")
    public ResponseEntity<String> getVoiceUsageAnalysis(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /usage/{}/analysis/voice?period={} - Getting voice usage analysis", userId, period);
        String analysis = usageService.getVoiceUsageAnalysis(userId, period);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Kullanıcının SMS kullanım analizini getirir
     */
    @GetMapping("/{userId}/analysis/sms")
    public ResponseEntity<String> getSMSUsageAnalysis(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /usage/{}/analysis/sms?period={} - Getting SMS usage analysis", userId, period);
        String analysis = usageService.getSMSUsageAnalysis(userId, period);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Kullanıcının roaming kullanım analizini getirir
     */
    @GetMapping("/{userId}/analysis/roaming")
    public ResponseEntity<String> getRoamingUsageAnalysis(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /usage/{}/analysis/roaming?period={} - Getting roaming usage analysis", userId, period);
        String analysis = usageService.getRoamingUsageAnalysis(userId, period);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Kullanıcının tüm kullanım analizlerini bir arada getirir
     */
    @GetMapping("/{userId}/analysis/all")
    public ResponseEntity<UsageSummaryDTO> getAllUsageAnalysis(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /usage/{}/analysis/all?period={} - Getting all usage analysis", userId, period);
        UsageSummaryDTO summary = usageService.getUsageSummary(userId, period);
        return ResponseEntity.ok(summary);
    }
}
