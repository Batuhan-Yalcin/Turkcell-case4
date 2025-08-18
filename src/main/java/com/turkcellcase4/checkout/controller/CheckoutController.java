package com.turkcellcase4.checkout.controller;

import com.turkcellcase4.checkout.dto.CheckoutRequestDTO;
import com.turkcellcase4.checkout.dto.CheckoutResponseDTO;
import com.turkcellcase4.checkout.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<CheckoutResponseDTO> process(@Valid @RequestBody CheckoutRequestDTO request) {
        log.info("POST /checkout - Process checkout");
        CheckoutResponseDTO response = checkoutService.processCheckout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/checkout/" + response.getOrderId() + "/status")
                .body(response);
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<CheckoutResponseDTO> status(@PathVariable String orderId) {
        log.info("GET /checkout/{}/status - Get order status", orderId);
        return ResponseEntity.ok(checkoutService.getOrderStatus(orderId));
    }

    @PostMapping("/validate")
    public ResponseEntity<CheckoutResponseDTO> validate(@Valid @RequestBody CheckoutRequestDTO request) {
        log.info("POST /checkout/validate - Validate scenario");
        return ResponseEntity.ok(checkoutService.validateScenario(request));
    }

    @PostMapping("/preview")
    public ResponseEntity<CheckoutResponseDTO> preview(@Valid @RequestBody CheckoutRequestDTO request) {
        log.info("POST /checkout/preview - Preview changes");
        return ResponseEntity.ok(checkoutService.previewChanges(request));
    }
}
