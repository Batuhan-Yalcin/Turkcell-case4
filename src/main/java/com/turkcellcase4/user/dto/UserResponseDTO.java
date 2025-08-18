package com.turkcellcase4.user.dto;

import com.turkcellcase4.common.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long userId;
    private String name;
    private Long currentPlanId;
    private UserType type;
    private String msisdn;
}
