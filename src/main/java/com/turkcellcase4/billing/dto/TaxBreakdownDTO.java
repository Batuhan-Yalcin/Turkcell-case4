package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxBreakdownDTO {
    
    private Long billId;
    private Long userId;
    private String period;
    
    // Toplam vergi bilgileri
    private BigDecimal totalTax;
    private BigDecimal totalAmount;
    private BigDecimal effectiveTaxRate;
    
    // Vergi türleri
    private BigDecimal kdvAmount;
    private BigDecimal oivAmount;
    private BigDecimal otherTaxes;
    
    // Kategori bazlı vergi dağılımı
    private Map<String, BigDecimal> categoryTaxes;
    
    // Birim maliyet analizi
    private BigDecimal costPerGB;
    private BigDecimal costPerMinute;
    private BigDecimal costPerSMS;
    
    // AI açıklaması
    private String aiExplanation;
    
    // Vergi optimizasyonu
    private String optimizationHint;
    private BigDecimal potentialTaxSavings;
    
    // Karşılaştırma (eğer iki fatura karşılaştırılıyorsa)
    private BigDecimal comparisonTaxRate;
    private String comparisonResult;
}
