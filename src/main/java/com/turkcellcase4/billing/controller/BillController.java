package com.turkcellcase4.billing.controller;

import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.dto.BillItemDTO;
import com.turkcellcase4.billing.dto.BillSummaryDTO;
import com.turkcellcase4.billing.dto.CreateBillRequestDTO;
import com.turkcellcase4.billing.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BillController {

    private final BillService billService;

    @GetMapping("/{billId}")
    public ResponseEntity<BillResponseDTO> getBillById(@PathVariable Long billId) {
        log.info("GET /bills/{} - Getting bill by ID", billId);
        BillResponseDTO bill = billService.getBillById(billId);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/user/{userId}/period")
    public ResponseEntity<BillResponseDTO> getBillByUserIdAndPeriod(
            @PathVariable Long userId,
            @RequestParam String period) {
        log.info("GET /bills/user/{}?period={} - Getting bill for user and period", userId, period);
        BillResponseDTO bill = billService.getBillByUserIdAndPeriod(userId, period);
        return ResponseEntity.ok(bill);
    }

    @GetMapping("/{userId}/recent")
    public ResponseEntity<List<BillResponseDTO>> getRecentBillsByUserId(@PathVariable Long userId) {
        log.info("GET /bills/{}/recent - Getting recent bills for user", userId);
        List<BillResponseDTO> bills = billService.getRecentBillsByUserId(userId);
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{userId}/periods")
    public ResponseEntity<List<String>> getAvailablePeriods(@PathVariable Long userId) {
        log.info("GET /bills/{}/periods - Getting available periods for user", userId);
        List<String> periods = billService.getAvailablePeriods(userId);
        return ResponseEntity.ok(periods);
    }

    @GetMapping("/{billId}/items")
    public ResponseEntity<List<BillItemDTO>> getBillItemsByBillId(@PathVariable Long billId) {
        log.info("GET /bills/{}/items - Getting bill items", billId);
        List<BillItemDTO> items = billService.getBillItemsByBillId(billId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{billId}/summary")
    public ResponseEntity<BillSummaryDTO> getBillSummary(@PathVariable Long billId) {
        log.info("GET /bills/{}/summary - Getting bill summary", billId);
        BillSummaryDTO summary = billService.getBillSummary(billId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{userId}/range")
    public ResponseEntity<List<BillResponseDTO>> getBillsByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /bills/{}/range?startDate={}&endDate={} - Getting bills in date range", userId, startDate, endDate);
        List<BillResponseDTO> bills = billService.getBillsByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(bills);
    }

    @PostMapping
    public ResponseEntity<BillResponseDTO> createBill(@Valid @RequestBody CreateBillRequestDTO request) {
        log.info("POST /bills - Creating new bill for user: {}", request.getUserId());
        BillResponseDTO bill = billService.createBill(request);
        return ResponseEntity.ok(bill);
    }
}
