package com.turkcellcase4.checkout.service;

import com.turkcellcase4.checkout.dto.CheckoutRequestDTO;
import com.turkcellcase4.checkout.dto.CheckoutResponseDTO;

public interface CheckoutService {
    
    CheckoutResponseDTO processCheckout(CheckoutRequestDTO request);
    
    CheckoutResponseDTO getOrderStatus(String orderId);
    
    CheckoutResponseDTO validateScenario(CheckoutRequestDTO request);
    
    CheckoutResponseDTO previewChanges(CheckoutRequestDTO request);
}
