package com.turkcellcase4.security.dto;

import com.turkcellcase4.common.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "İsim zorunludur")
    private String name;
    
    @NotBlank(message = "MSISDN zorunludur")
    private String msisdn;
    
    private UserType userType;
    
    private Long currentPlanId;
}
