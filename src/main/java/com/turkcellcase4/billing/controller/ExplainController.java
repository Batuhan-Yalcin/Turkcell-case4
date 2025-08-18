package com.turkcellcase4.billing.controller;

import com.turkcellcase4.billing.dto.ExplainRequestDTO;
import com.turkcellcase4.billing.dto.ExplainResponseDTO;
import com.turkcellcase4.billing.dto.BillSummaryDTO;
import com.turkcellcase4.billing.dto.CategoryBreakdownDTO;
import com.turkcellcase4.billing.service.ExplainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/explain")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExplainController {

    private final ExplainService explainService;

    @PostMapping
    public ResponseEntity<ExplainResponseDTO> explainBill(@Valid @RequestBody ExplainRequestDTO request) {
        log.info("POST /explain - Explaining bill: {}", request.getBillId());
        ExplainResponseDTO response = explainService.explainBill(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{billId}/summary")
    public ResponseEntity<BillSummaryDTO> getBillSummary(@PathVariable Long billId) {
        log.info("GET /explain/{}/summary - Getting bill summary", billId);
        BillSummaryDTO summary = explainService.getBillSummary(billId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{billId}/breakdown")
    public ResponseEntity<CategoryBreakdownDTO> getCategoryBreakdown(
            @PathVariable Long billId,
            @RequestParam String category) {
        log.info("GET /explain/{}/breakdown?category={} - Getting category breakdown", billId, category);
        CategoryBreakdownDTO breakdown = explainService.getCategoryBreakdown(billId, category);
        return ResponseEntity.ok(breakdown);
    }
}
