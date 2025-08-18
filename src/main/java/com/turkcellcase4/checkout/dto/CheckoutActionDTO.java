package com.turkcellcase4.checkout.dto;

import com.turkcellcase4.common.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutActionDTO {
    private ActionType type;
    private Map<String, Object> payload;
}
