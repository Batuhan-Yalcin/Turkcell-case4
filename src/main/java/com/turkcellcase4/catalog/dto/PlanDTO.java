package com.turkcellcase4.catalog.dto;

import com.turkcellcase4.common.enums.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDTO {
    private Long planId;
    private String planName;
    private PlanType type;
    private Double quotaGb;
    private Integer quotaMin;
    private Integer quotaSms;
    private BigDecimal monthlyPrice;
    private BigDecimal overageGb;
    private BigDecimal overageMin;
    private BigDecimal overageSms;
}
