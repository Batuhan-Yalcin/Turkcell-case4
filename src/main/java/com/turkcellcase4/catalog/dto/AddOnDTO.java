package com.turkcellcase4.catalog.dto;

import com.turkcellcase4.common.enums.AddOnType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddOnDTO {
    private Long addonId;
    private String name;
    private AddOnType type;
    private Double extraGb;
    private Integer extraMin;
    private Integer extraSms;
    private BigDecimal price;
}
