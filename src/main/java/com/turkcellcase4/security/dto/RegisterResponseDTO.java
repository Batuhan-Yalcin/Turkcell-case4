package com.turkcellcase4.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String msisdn;
    private String role;
    private String message;
}
