package com.turkcellcase4.catalog.service.impl;

import com.turkcellcase4.catalog.dto.CatalogResponseDTO;
import com.turkcellcase4.catalog.dto.PlanDTO;
import com.turkcellcase4.catalog.dto.AddOnDTO;
import com.turkcellcase4.catalog.dto.VASDTO;
import com.turkcellcase4.catalog.dto.PremiumSMSDTO;
import com.turkcellcase4.catalog.mapper.CatalogMapper;
import com.turkcellcase4.catalog.model.Plan;
import com.turkcellcase4.catalog.model.AddOnPack;
import com.turkcellcase4.catalog.model.VAS;
import com.turkcellcase4.catalog.model.PremiumSMS;
import com.turkcellcase4.catalog.repository.PlanRepository;
import com.turkcellcase4.catalog.repository.AddOnPackRepository;
import com.turkcellcase4.catalog.repository.VASRepository;
import com.turkcellcase4.catalog.repository.PremiumSMSRepository;
import com.turkcellcase4.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogServiceImpl implements CatalogService {

    private final PlanRepository planRepository;
    private final AddOnPackRepository addOnPackRepository;
    private final VASRepository vasRepository;
    private final PremiumSMSRepository premiumSMSRepository;
    private final CatalogMapper catalogMapper;

    @Override
    public CatalogResponseDTO getFullCatalog() {
        log.info("Getting full catalog");
        
        List<Plan> plans = planRepository.findAll();
        List<AddOnPack> addOns = addOnPackRepository.findAll();
        List<VAS> vasServices = vasRepository.findAll();
        List<PremiumSMS> premiumSMSServices = premiumSMSRepository.findAll();
        
        return CatalogResponseDTO.builder()
                .plans(catalogMapper.toPlanDTOList(plans))
                .addons(catalogMapper.toAddOnDTOList(addOns))
                .vas(catalogMapper.toVASDTOList(vasServices))
                .premiumSms(catalogMapper.toPremiumSMSDTOList(premiumSMSServices))
                .build();
    }

    @Override
    public List<PlanDTO> getPlans() {
        log.info("Getting all plans");
        List<Plan> plans = planRepository.findAll();
        return catalogMapper.toPlanDTOList(plans);
    }

    @Override
    public List<AddOnDTO> getAddOns() {
        log.info("Getting all add-ons");
        List<AddOnPack> addOns = addOnPackRepository.findAll();
        return catalogMapper.toAddOnDTOList(addOns);
    }

    @Override
    public List<VASDTO> getVAS() {
        log.info("Getting all VAS services");
        List<VAS> vasServices = vasRepository.findAll();
        return catalogMapper.toVASDTOList(vasServices);
    }

    @Override
    public List<PremiumSMSDTO> getPremiumSMS() {
        log.info("Getting all Premium SMS services");
        List<PremiumSMS> premiumSMSServices = premiumSMSRepository.findAll();
        return catalogMapper.toPremiumSMSDTOList(premiumSMSServices);
    }
}
