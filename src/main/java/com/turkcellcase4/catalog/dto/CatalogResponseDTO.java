package com.turkcellcase4.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponseDTO {
    private List<PlanDTO> plans;
    private List<AddOnDTO> addons;
    private List<VASDTO> vas;
    private List<PremiumSMSDTO> premiumSms;
}
