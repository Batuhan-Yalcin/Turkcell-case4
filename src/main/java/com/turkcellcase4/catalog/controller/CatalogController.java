package com.turkcellcase4.catalog.controller;

import com.turkcellcase4.catalog.dto.*;
import com.turkcellcase4.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<CatalogResponseDTO> getFullCatalog() {
        log.info("GET /catalog - Fetching full catalog");
        return ResponseEntity.ok(catalogService.getFullCatalog());
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanDTO>> getPlans() {
        log.info("GET /catalog/plans - Fetching plans");
        return ResponseEntity.ok(catalogService.getPlans());
    }

    @GetMapping("/addons")
    public ResponseEntity<List<AddOnDTO>> getAddOns() {
        log.info("GET /catalog/addons - Fetching add-ons");
        return ResponseEntity.ok(catalogService.getAddOns());
    }

    @GetMapping("/vas")
    public ResponseEntity<List<VASDTO>> getVAS() {
        log.info("GET /catalog/vas - Fetching VAS");
        return ResponseEntity.ok(catalogService.getVAS());
    }

    @GetMapping("/premium-sms")
    public ResponseEntity<List<PremiumSMSDTO>> getPremiumSMS() {
        log.info("GET /catalog/premium-sms - Fetching Premium SMS");
        return ResponseEntity.ok(catalogService.getPremiumSMS());
    }
}
