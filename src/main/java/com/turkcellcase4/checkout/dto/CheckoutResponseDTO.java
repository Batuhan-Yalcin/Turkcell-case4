package com.turkcellcase4.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {
    private String status;
    private String orderId;
    private Long userId;
    private List<String> processedActions;
    private List<String> validationResults;
    private LocalDateTime timestamp;
    private String message;
    private Map<String, Object> orderDetails;
}
