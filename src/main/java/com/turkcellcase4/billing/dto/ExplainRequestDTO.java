package com.turkcellcase4.billing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplainRequestDTO {
    
    @NotNull(message = "Bill ID is required")
    private Long billId;
}
