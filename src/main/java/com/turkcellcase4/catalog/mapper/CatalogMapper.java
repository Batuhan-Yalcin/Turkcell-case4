package com.turkcellcase4.catalog.mapper;

import com.turkcellcase4.catalog.dto.*;
import com.turkcellcase4.catalog.model.*;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogMapper {
    PlanDTO toPlanDTO(Plan plan);
    AddOnDTO toAddOnDTO(AddOnPack addOnPack);
    VASDTO toVASDTO(VAS vas);
    PremiumSMSDTO toPremiumSMSDTO(PremiumSMS premiumSMS);

    List<PlanDTO> toPlanDTOList(List<Plan> plans);
    List<AddOnDTO> toAddOnDTOList(List<AddOnPack> addOnPacks);
    List<VASDTO> toVASDTOList(List<VAS> vasList);
    List<PremiumSMSDTO> toPremiumSMSDTOList(List<PremiumSMS> premiumSMSList);
}
