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
import java.util.HashMap;
import java.util.Map;

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
        
        // Generate detailed response
        Map<String, Object> orderDetails = generateOrderDetails(request, orderId);
        
        return CheckoutResponseDTO.builder()
                .status("ok")
                .orderId(orderId)
                .message("Checkout işlemi başarıyla tamamlandı")
                .orderDetails(orderDetails)
                .build();
    }

    @Override
    public CheckoutResponseDTO getOrderStatus(String orderId) {
        log.info("Getting order status for: {}", orderId);
        
        // Mock order status with different states
        String status = getMockOrderStatus(orderId);
        String message = getStatusMessage(status);
        
        return CheckoutResponseDTO.builder()
                .status(status)
                .orderId(orderId)
                .message(message)
                .orderDetails(generateStatusDetails(orderId, status))
                .build();
    }

    @Override
    public CheckoutResponseDTO validateScenario(CheckoutRequestDTO request) {
        log.info("Validating scenario for user: {}", request.getUserId());
        
        // Validate actions
        List<String> validationResults = validateActions(request.getActions());
        
        // Check if scenario is feasible
        boolean isFeasible = checkScenarioFeasibility(request.getActions());
        
        String validationMessage = isFeasible ? 
            "Senaryo doğrulandı ve uygulanabilir" : 
            "Senaryo doğrulandı ancak bazı kısıtlamalar mevcut";
        
        return CheckoutResponseDTO.builder()
                .status("validated")
                .orderId("VALID-" + UUID.randomUUID())
                .message(validationMessage)
                .orderDetails(generateValidationDetails(validationResults, isFeasible))
                .build();
    }

    @Override
    public CheckoutResponseDTO previewChanges(CheckoutRequestDTO request) {
        log.info("Previewing changes for user: {}", request.getUserId());
        
        // Calculate estimated costs and savings
        Map<String, Object> previewData = calculatePreviewData(request.getActions());
        
        return CheckoutResponseDTO.builder()
                .status("preview")
                .orderId("PREVIEW-" + UUID.randomUUID())
                .message("Değişiklik önizlemesi hazırlandı")
                .orderDetails(previewData)
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
            return List.of("İşlenecek eylem bulunamadı");
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
            return List.of("Doğrulama: Eylem belirtilmedi");
        }
        
        return actions.stream()
                .map(this::validateAction)
                .toList();
    }

    private String validateAction(CheckoutActionDTO action) {
        return switch (action.getType()) {
            case CHANGE_PLAN -> "Doğrulama: Plan değişikliği uygun";
            case ADD_ADDON -> "Doğrulama: Ek paket ekleme uygun";
            case CANCEL_VAS -> "Doğrulama: VAS iptal uygun";
            case BLOCK_PREMIUM_SMS -> "Doğrulama: Premium SMS engelleme uygun";
            default -> "Doğrulama: Bilinmeyen işlem türü";
        };
    }

    private boolean checkScenarioFeasibility(List<CheckoutActionDTO> actions) {
        if (actions == null || actions.isEmpty()) {
            return true;
        }
        
        // Check for conflicting actions
        boolean hasPlanChange = actions.stream().anyMatch(a -> a.getType() == ActionType.CHANGE_PLAN);
        boolean hasAddon = actions.stream().anyMatch(a -> a.getType() == ActionType.ADD_ADDON);
        
        // If changing plan and adding addon, check compatibility
        if (hasPlanChange && hasAddon) {
            // Mock compatibility check
            return Math.random() > 0.3; // 70% chance of compatibility
        }
        
        return true;
    }

    private Map<String, Object> generateOrderDetails(CheckoutRequestDTO request, String orderId) {
        Map<String, Object> details = new HashMap<>();
        details.put("orderId", orderId);
        details.put("userId", request.getUserId());
        details.put("timestamp", LocalDateTime.now().toString());
        details.put("actions", request.getActions());
        details.put("estimatedProcessingTime", "2-3 iş günü");
        details.put("nextBillingCycle", "Bir sonraki fatura döneminde aktif olacak");
        
        return details;
    }

    private String getMockOrderStatus(String orderId) {
        // Mock different order statuses based on order ID
        if (orderId.contains("MOCK-FT")) {
            int lastDigit = Integer.parseInt(orderId.substring(orderId.length() - 1));
            if (lastDigit < 3) return "processing";
            else if (lastDigit < 6) return "completed";
            else if (lastDigit < 9) return "failed";
            else return "pending";
        }
        return "unknown";
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "processing" -> "Sipariş işleniyor";
            case "completed" -> "Sipariş tamamlandı";
            case "failed" -> "Sipariş başarısız";
            case "pending" -> "Sipariş beklemede";
            default -> "Sipariş durumu bilinmiyor";
        };
    }

    private Map<String, Object> generateStatusDetails(String orderId, String status) {
        Map<String, Object> details = new HashMap<>();
        details.put("orderId", orderId);
        details.put("status", status);
        details.put("lastUpdated", LocalDateTime.now().toString());
        
        switch (status) {
            case "processing" -> {
                details.put("progress", "60%");
                details.put("estimatedCompletion", "1-2 saat");
            }
            case "completed" -> {
                details.put("completionTime", LocalDateTime.now().minusHours(2).toString());
                details.put("nextSteps", "Değişiklikler bir sonraki fatura döneminde aktif olacak");
            }
            case "failed" -> {
                details.put("failureReason", "Teknik bir hata oluştu");
                details.put("retryAvailable", true);
            }
            case "pending" -> {
                details.put("queuePosition", "5");
                details.put("estimatedStart", "30 dakika");
            }
        }
        
        return details;
    }

    private Map<String, Object> generateValidationDetails(List<String> validationResults, boolean isFeasible) {
        Map<String, Object> details = new HashMap<>();
        details.put("validationResults", validationResults);
        details.put("isFeasible", isFeasible);
        details.put("validationTimestamp", LocalDateTime.now().toString());
        details.put("estimatedSavings", "15-25 TL");
        details.put("implementationTime", "2-3 iş günü");
        
        if (!isFeasible) {
            details.put("warnings", List.of(
                "Plan değişikliği ile ek paket ekleme aynı anda yapılamaz",
                "Bazı VAS servisleri plan değişikliği ile uyumsuz olabilir"
            ));
        }
        
        return details;
    }

    private Map<String, Object> calculatePreviewData(List<CheckoutActionDTO> actions) {
        Map<String, Object> preview = new HashMap<>();
        
        if (actions == null || actions.isEmpty()) {
            preview.put("estimatedSavings", "0 TL");
            preview.put("newMonthlyCost", "Mevcut maliyet");
            preview.put("changes", "Değişiklik yok");
            return preview;
        }
        
        // Mock calculations
        double totalSavings = 0.0;
        double newCost = 0.0;
        
        for (CheckoutActionDTO action : actions) {
            switch (action.getType()) {
                case CHANGE_PLAN -> {
                    totalSavings += 15.0;
                    newCost += 25.0;
                }
                case ADD_ADDON -> {
                    totalSavings += 8.0;
                    newCost += 12.0;
                }
                case CANCEL_VAS -> {
                    totalSavings += 22.0;
                    newCost += 0.0;
                }
                case BLOCK_PREMIUM_SMS -> {
                    totalSavings += 18.0;
                    newCost += 0.0;
                }
            }
        }
        
        preview.put("estimatedSavings", totalSavings + " TL");
        preview.put("newMonthlyCost", newCost + " TL");
        preview.put("changes", actions.size() + " değişiklik");
        preview.put("roi", "3-6 ay içinde yatırım geri dönüşü");
        
        return preview;
    }
}
