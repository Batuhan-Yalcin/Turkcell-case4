package com.turkcellcase4.checkout.service.impl;

import com.turkcellcase4.checkout.dto.CheckoutRequestDTO;
import com.turkcellcase4.checkout.dto.CheckoutResponseDTO;
import com.turkcellcase4.checkout.dto.CheckoutActionDTO;
import com.turkcellcase4.checkout.service.CheckoutService;
import com.turkcellcase4.common.enums.ActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    @Override
    public CheckoutResponseDTO processCheckout(CheckoutRequestDTO request) {
        log.info("Processing checkout for user: {}", request.getUserId());
        
        // Generate mock order ID in MOCK-FT-123 format
        String orderId = generateMockOrderId();
        
        // Process actions
        List<String> processedActions = processActions(request.getActions());
        
        return CheckoutResponseDTO.builder()
                .status("ok")
                .orderId(orderId)
                .message("Checkout processed successfully")
                .build();
    }

    @Override
    public CheckoutResponseDTO getOrderStatus(String orderId) {
        log.info("Getting order status for: {}", orderId);
        
        // Mock order status
        return CheckoutResponseDTO.builder()
                .status("completed")
                .orderId(orderId)
                .message("Order completed successfully")
                .build();
    }

    @Override
    public CheckoutResponseDTO validateScenario(CheckoutRequestDTO request) {
        log.info("Validating scenario for user: {}", request.getUserId());
        
        // Validate actions
        List<String> validationResults = validateActions(request.getActions());
        
        return CheckoutResponseDTO.builder()
                .status("validated")
                .orderId("VALID-" + UUID.randomUUID())
                .message("Scenario validation completed")
                .build();
    }

    private String generateMockOrderId() {
        // Generate MOCK-FT-123 format order ID
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        return "MOCK-FT-" + timestamp.substring(0, 8) + randomSuffix;
    }

    private List<String> processActions(List<CheckoutActionDTO> actions) {
        if (actions == null || actions.isEmpty()) {
            return List.of("No actions to process");
        }
        
        return actions.stream()
                .map(this::processAction)
                .toList();
    }

    private String processAction(CheckoutActionDTO action) {
        return switch (action.getType()) {
            case CHANGE_PLAN -> "Plan değişikliği işlemi tamamlandı";
            case ADD_ADDON -> "Ek paket ekleme işlemi tamamlandı";
            case CANCEL_VAS -> "VAS iptal işlemi tamamlandı";
            case BLOCK_PREMIUM_SMS -> "Premium SMS engelleme işlemi tamamlandı";
            default -> "Bilinmeyen işlem türü";
        };
    }

    private List<String> validateActions(List<CheckoutActionDTO> actions) {
        if (actions == null || actions.isEmpty()) {
            return List.of("Validation: No actions provided");
        }
        
        return actions.stream()
                .map(this::validateAction)
                .toList();
    }

    private String validateAction(CheckoutActionDTO action) {
        return switch (action.getType()) {
            case CHANGE_PLAN -> "Validation: Plan değişikliği uygun";
            case ADD_ADDON -> "Validation: Ek paket ekleme uygun";
            case CANCEL_VAS -> "Validation: VAS iptal uygun";
            case BLOCK_PREMIUM_SMS -> "Validation: Premium SMS engelleme uygun";
            default -> "Validation: Bilinmeyen işlem türü";
        };
    }
}
