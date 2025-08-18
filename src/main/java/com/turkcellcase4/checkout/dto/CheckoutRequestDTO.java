package com.turkcellcase4.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Actions are required")
    private List<CheckoutActionDTO> actions;
}
