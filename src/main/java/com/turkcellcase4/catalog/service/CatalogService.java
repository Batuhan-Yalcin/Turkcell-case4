package com.turkcellcase4.catalog.service;

import com.turkcellcase4.catalog.dto.CatalogResponseDTO;
import com.turkcellcase4.catalog.dto.PlanDTO;
import com.turkcellcase4.catalog.dto.AddOnDTO;
import com.turkcellcase4.catalog.dto.VASDTO;
import com.turkcellcase4.catalog.dto.PremiumSMSDTO;

import java.util.List;

public interface CatalogService {
    
    CatalogResponseDTO getFullCatalog();
    
    List<PlanDTO> getPlans();
    
    List<AddOnDTO> getAddOns();
    
    List<VASDTO> getVAS();
    
    List<PremiumSMSDTO> getPremiumSMS();
}
