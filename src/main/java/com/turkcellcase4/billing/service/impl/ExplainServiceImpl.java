package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.dto.*;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.repository.BillItemRepository;
import com.turkcellcase4.billing.service.ExplainService;
import com.turkcellcase4.billing.service.UsageService;
import com.turkcellcase4.billing.service.LLMExplanationService;
import com.turkcellcase4.catalog.model.PremiumSMS;
import com.turkcellcase4.catalog.model.VAS;
import com.turkcellcase4.catalog.repository.PremiumSMSRepository;
import com.turkcellcase4.catalog.repository.VASRepository;
import com.turkcellcase4.common.enums.ItemCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.turkcellcase4.common.exception.BusinessLogicException;
import com.turkcellcase4.common.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExplainServiceImpl implements ExplainService {

    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PremiumSMSRepository premiumSMSRepository;
    private final VASRepository vasRepository;
    private final UsageService usageService;
    private final LLMExplanationService llmExplanationService;

    @Override
    public ExplainResponseDTO explainBill(ExplainRequestDTO request) {
        log.info("Explaining bill: {}", request.getBillId());
        
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Fatura bulunamadı: " + request.getBillId()));
        
        BillSummaryDTO summary = getBillSummary(request.getBillId());
        List<CategoryBreakdownDTO> breakdown = getCategoryBreakdowns(request.getBillId());
        
        log.info("Bill items count: {}", breakdown.size());
        log.info("Breakdown categories: {}", breakdown.stream().map(c -> c.getCategory().name()).collect(Collectors.toList()));
        
        String naturalLanguageSummary = generateNaturalLanguageSummary(bill, breakdown);
        
        log.info("Generated natural language summary: {}", naturalLanguageSummary);
        
        return ExplainResponseDTO.builder()
                .summary(summary)
                .breakdown(breakdown)
                .naturalLanguageSummary(naturalLanguageSummary)
                .build();
    }

    @Override
    public BillSummaryDTO getBillSummary(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Fatura bulunamadı: " + billId));
        
        try {
            List<BillItem> items = billItemRepository.findByBill_BillId(billId);
            
            BigDecimal totalAmount = bill.getTotalAmount();
            BigDecimal taxes = items.stream()
                    .filter(item -> ItemCategory.TAX.equals(item.getCategory()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal usageBasedCharges = items.stream()
                    .filter(item -> ItemCategory.DATA.equals(item.getCategory()) || 
                                   ItemCategory.VOICE.equals(item.getCategory()) || 
                                   ItemCategory.SMS.equals(item.getCategory()) ||
                                   ItemCategory.ROAMING.equals(item.getCategory()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal oneTimeCharges = items.stream()
                    .filter(item -> ItemCategory.ONE_OFF.equals(item.getCategory()))
                    .map(BillItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            String savingsHint = calculateSavingsHint(items);
            
            return BillSummaryDTO.builder()
                    .totalAmount(totalAmount)
                    .taxes(taxes)
                    .usageBasedCharges(usageBasedCharges)
                    .oneTimeCharges(oneTimeCharges)
                    .savingsHint(savingsHint)
                    .build();
        } catch (Exception e) {
            throw new BusinessLogicException("Fatura özeti oluşturma hatası: " + e.getMessage());
        }
    }

    @Override
    public List<CategoryBreakdownDTO> getCategoryBreakdowns(Long billId) {
        List<BillItem> items = billItemRepository.findByBill_BillId(billId);
        log.info("Found {} bill items for bill {}", items.size(), billId);
        
        if (items.isEmpty()) {
            log.warn("No bill items found for bill {}", billId);
            return new ArrayList<>();
        }
        
        Map<ItemCategory, List<BillItem>> groupedItems = items.stream()
                .collect(Collectors.groupingBy(BillItem::getCategory));
        
        log.info("Grouped items by category: {}", groupedItems.keySet());
        
        return groupedItems.entrySet().stream()
                .map(entry -> createCategoryBreakdown(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryBreakdownDTO getCategoryBreakdown(Long billId, String category) {
        ItemCategory itemCategory = ItemCategory.valueOf(category.toUpperCase());
        List<BillItem> items = billItemRepository.findByBill_BillIdAndCategory(billId, itemCategory);
        
        return createCategoryBreakdown(itemCategory, items);
    }

    private CategoryBreakdownDTO createCategoryBreakdown(ItemCategory category, List<BillItem> items) {
        BigDecimal total = items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<BillItemDTO> lines = items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        String explanation = generateCategoryExplanation(category, items);
        
        return CategoryBreakdownDTO.builder()
                .category(category)
                .total(total)
                .lines(lines)
                .explanation(explanation)
                .build();
    }

    private String generateCategoryExplanation(ItemCategory category, List<BillItem> items) {
        switch (category) {
            case DATA:
                return generateDataExplanation(items);
            case VOICE:
                return generateVoiceExplanation(items);
            case SMS:
                return generateSMSExplanation(items);
            case ROAMING:
                return generateRoamingExplanation(items);
            case PREMIUM_SMS:
                return generatePremiumSMSExplanation(items);
            case VAS:
                return generateVASExplanation(items);
            case TAX:
                return "Vergi kalemleri";
            case DISCOUNT:
                return "İndirim kalemleri";
            case ONE_OFF:
                return "Tek seferlik ücretler";
            default:
                return "Diğer kalemler";
        }
    }

    private String generateDataExplanation(List<BillItem> items) {
        if (items.isEmpty()) return "Data kullanımı yok";
        
        BigDecimal totalData = items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return String.format("Toplam data ücreti: %s TL", totalData);
    }

    private String generateVoiceExplanation(List<BillItem> items) {
        if (items.isEmpty()) return "Ses kullanımı yok";
        
        BigDecimal totalVoice = items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return String.format("Toplam ses ücreti: %s TL", totalVoice);
    }

    private String generateSMSExplanation(List<BillItem> items) {
        if (items.isEmpty()) return "SMS kullanımı yok";
        
        BigDecimal totalSMS = items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return String.format("Toplam SMS ücreti: %s TL", totalSMS);
    }

    private String generateRoamingExplanation(List<BillItem> items) {
        if (items.isEmpty()) return "Roaming kullanımı yok";
        
        BigDecimal totalRoaming = items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return String.format("Toplam roaming ücreti: %s TL", totalRoaming);
    }

    private String generatePremiumSMSExplanation(List<BillItem> items) {
        if (items.isEmpty()) return "Premium SMS kullanımı yok";
        
        BigDecimal totalPremiumSMS = items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return String.format("Toplam Premium SMS ücreti: %s TL", totalPremiumSMS);
    }

    private String generateVASExplanation(List<BillItem> items) {
        if (items.isEmpty()) return "VAS kullanımı yok";
        
        BigDecimal totalVAS = items.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return String.format("Toplam VAS ücreti: %s TL", totalVAS);
    }

    private BillItemDTO convertToDTO(BillItem item) {
        return BillItemDTO.builder()
                .itemId(item.getItemId())
                .category(item.getCategory())
                .subtype(item.getSubtype())
                .description(item.getDescription())
                .amount(item.getAmount())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .taxRate(item.getTaxRate())
                .build();
    }

    private String generateItemLine(BillItem item) {
        switch (item.getCategory()) {
            case DATA:
                return generateDataLine(item);
            case PREMIUM_SMS:
                return generatePremiumSMSLine(item);
            case VAS:
                return generateVASLine(item);
            case VOICE:
                return generateVoiceLine(item);
            case SMS:
                return generateSMSLine(item);
            case ROAMING:
                return generateRoamingLine(item);
            case TAX:
                return generateTaxLine(item);
            case DISCOUNT:
                return generateDiscountLine(item);
            case ONE_OFF:
                return generateOneOffLine(item);
            default:
                return String.format("%s: %s TL", item.getDescription(), item.getAmount());
        }
    }

    private String generateDataLine(BillItem item) {
        if ("data_overage".equals(item.getSubtype())) {
            BigDecimal overageGB = BigDecimal.valueOf(item.getQuantity());
            BigDecimal unitPrice = item.getUnitPrice();
            // Tarih bilgisi varsa ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da %.1f GB aşım → %.1f×%.2f TL = %.2f TL", 
                        dateInfo, overageGB, overageGB, unitPrice, item.getAmount());
            } else {
                return String.format("Ay içinde %.1f GB aşım → %.1f×%.2f TL = %.2f TL", 
                        overageGB, overageGB, unitPrice, item.getAmount());
            }
        } else {
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da data kullanımı: %s - %.2f TL", 
                        dateInfo, item.getDescription(), item.getAmount());
            } else {
                return String.format("Data kullanımı: %s - %.2f TL", item.getDescription(), item.getAmount());
            }
        }
    }

    private String generatePremiumSMSLine(BillItem item) {
        // Try to find premium SMS details from catalog
        Optional<PremiumSMS> premiumSMS = premiumSMSRepository.findById(item.getSubtype());
        
        if (premiumSMS.isPresent()) {
            PremiumSMS sms = premiumSMS.get();
            // Tarih bazlı detaylı açıklama ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da %s×%s numarasına Premium SMS → %s×%.2f TL (sağlayıcı: %s)", 
                        dateInfo, item.getQuantity(), sms.getShortcode(), item.getQuantity(), 
                        sms.getUnitPrice(), sms.getProvider());
            } else {
                return String.format("%s numarasına %s SMS → %s×%.2f TL (sağlayıcı: %s)", 
                        sms.getShortcode(), item.getQuantity(), item.getQuantity(), 
                        sms.getUnitPrice(), sms.getProvider());
            }
        } else {
            // Tarih bilgisi varsa ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da Premium SMS: %s - %s adet - %.2f TL", 
                        dateInfo, item.getDescription(), item.getQuantity(), item.getAmount());
            } else {
                return String.format("Premium SMS: %s - %s adet - %.2f TL", 
                        item.getDescription(), item.getQuantity(), item.getAmount());
            }
        }
    }

    private String generateVASLine(BillItem item ) {
        if ("plan_fee".equals(item.getSubtype())) {
            return String.format("Plan ücreti: %s - %.2f TL", item.getDescription(), item.getAmount());
        }
        
        // Try to find VAS details from catalog
        Optional<VAS> vas = vasRepository.findById(Long.valueOf(item.getSubtype()));
        
        if (vas.isPresent()) {
            VAS vasService = vas.get();
            // Tarih bilgisi varsa ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da %s servisi aylık ücret %.2f TL", 
                        dateInfo, vasService.getName(), vasService.getMonthlyFee());
            } else {
                return String.format("%s servisi aylık ücret %.2f TL", vasService.getName(), vasService.getMonthlyFee());
            }
        } else {
            // Tarih bilgisi varsa ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da VAS: %s - %.2f TL", 
                        dateInfo, item.getDescription(), item.getAmount());
            } else {
                return String.format("VAS: %s - %.2f TL", item.getDescription(), item.getAmount());
            }
        }
    }

    private String generateVoiceLine(BillItem item) {
        if ("voice_overage".equals(item.getSubtype())) {
            BigDecimal overageMinutes = BigDecimal.valueOf(item.getQuantity());
            BigDecimal unitPrice = item.getUnitPrice();
            // Tarih bilgisi varsa ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da %.0f dk arama aşımı → %.0f×%.2f TL = %.2f TL", 
                        dateInfo, overageMinutes, overageMinutes, unitPrice, item.getAmount());
            } else {
                return String.format("Ay içinde %.0f dk arama aşımı → %.0f×%.2f TL = %.2f TL", 
                        overageMinutes, overageMinutes, unitPrice, item.getAmount());
            }
        } else if ("intl_call".equals(item.getSubtype())) {
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da uluslararası arama: %s - %.2f TL", 
                        dateInfo, item.getDescription(), item.getAmount());
            } else {
                return String.format("Uluslararası arama: %s - %.2f TL", item.getDescription(), item.getAmount());
            }
        } else {
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da ses kullanımı: %s - %.2f TL", 
                        dateInfo, item.getDescription(), item.getAmount());
            } else {
                return String.format("Ses kullanımı: %s - %.2f TL", item.getDescription(), item.getAmount());
            }
        }
    }

    private String generateSMSLine(BillItem item) {
        if ("sms_overage".equals(item.getSubtype())) {
            BigDecimal overageSMS = BigDecimal.valueOf(item.getQuantity());
            BigDecimal unitPrice = item.getUnitPrice();
            // Tarih bilgisi varsa ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da %s SMS aşımı → %s×%.2f TL = %.2f TL", 
                        dateInfo, overageSMS, overageSMS, unitPrice, item.getAmount());
            } else {
                return String.format("Ay içinde %s SMS aşımı → %s×%.2f TL = %.2f TL", 
                        overageSMS, overageSMS, unitPrice, item.getAmount());
            }
        } else {
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da SMS kullanımı: %s - %s adet - %.2f TL", 
                        dateInfo, item.getDescription(), item.getQuantity(), item.getAmount());
            } else {
                return String.format("SMS kullanımı: %s - %s adet - %.2f TL", 
                        item.getDescription(), item.getQuantity(), item.getAmount());
            }
        }
    }

    private String generateRoamingLine(BillItem item) {
        if ("roaming_data".equals(item.getSubtype())) {
            BigDecimal roamingMB = BigDecimal.valueOf(item.getQuantity());
            BigDecimal unitPrice = item.getUnitPrice();
            // Tarih bilgisi varsa ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da yurt dışı data: %.0f MB → %.0f×%.2f TL = %.2f TL", 
                        dateInfo, roamingMB, roamingMB, unitPrice, item.getAmount());
            } else {
                return String.format("Yurt dışı data: %.0f MB → %.0f×%.2f TL = %.2f TL", 
                        roamingMB, roamingMB, unitPrice, item.getAmount());
            }
        } else if ("roaming_voice".equals(item.getSubtype())) {
            BigDecimal roamingMinutes = BigDecimal.valueOf(item.getQuantity());
            BigDecimal unitPrice = item.getUnitPrice();
            // Tarih bilgisi varsa ekle
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da yurt dışı arama: %.0f dk → %.0f×%.2f TL = %.2f TL", 
                        dateInfo, roamingMinutes, roamingMinutes, unitPrice, item.getAmount());
            } else {
                return String.format("Yurt dışı arama: %.0f dk → %.0f×%.2f TL = %.2f TL", 
                        roamingMinutes, roamingMinutes, unitPrice, item.getAmount());
            }
        } else {
            String dateInfo = getDateInfoForItem(item);
            if (dateInfo != null) {
                return String.format("%s'da roaming: %s - %.2f TL", 
                        dateInfo, item.getDescription(), item.getAmount());
            } else {
                return String.format("Roaming: %s - %.2f TL", item.getDescription(), item.getAmount());
            }
        }
    }

    private String generateTaxLine(BillItem item) {
        return String.format("Vergi (%s%%): %.2f TL", 
                item.getTaxRate().multiply(new BigDecimal("100")), item.getAmount());
    }

    private String generateDiscountLine(BillItem item) {
        return String.format("İndirim: %s - %.2f TL", item.getDescription(), item.getAmount());
    }

    private String generateOneOffLine(BillItem item) {
        return String.format("Tek seferlik: %s - %.2f TL", item.getDescription(), item.getAmount());
    }

    private String generateNaturalLanguageSummary(Bill bill, List<CategoryBreakdownDTO> breakdown) {
        try {
            // LLM servisini kullanarak AI destekli özet üret
            String period = bill.getPeriodStart().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            
            // Ana kategorileri string olarak birleştir
            String mainCategories = breakdown.stream()
                    .filter(cat -> cat.getTotal().compareTo(BigDecimal.ZERO) > 0)
                    .map(cat -> cat.getCategory().name().toLowerCase())
                    .collect(Collectors.joining(", "));
            
            // LLM servisini çağır
            String aiSummary = llmExplanationService.generateBillAnalysisSummary(
                bill.getBillId(), 
                period, 
                bill.getTotalAmount().doubleValue(), 
                mainCategories
            );
            
            log.info("AI generated summary: {}", aiSummary);
            return aiSummary;
            
        } catch (Exception e) {
            log.warn("AI özeti üretilemedi, fallback kullanılıyor: {}", e.getMessage());
            return generateFallbackSummary(bill, breakdown);
        }
    }

    private String generateFallbackSummary(Bill bill, List<CategoryBreakdownDTO> breakdown) {
        StringBuilder summary = new StringBuilder();
        
        // Calculate totals by category
        Map<String, BigDecimal> categoryTotals = breakdown.stream()
                .collect(Collectors.toMap(
                    category -> category.getCategory().name().toLowerCase(),
                    CategoryBreakdownDTO::getTotal
                ));
        
        try {
            // Kullanım verilerini al
            String period = bill.getPeriodStart().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String usageAnalysis = usageService.getDataUsageAnalysis(bill.getUser().getUserId(), period);
            if (usageAnalysis != null && !usageAnalysis.equals("Bu dönemde data kullanımı bulunmuyor.")) {
                summary.append(usageAnalysis).append(" ");
            }
        } catch (Exception e) {
            log.warn("Kullanım verileri alınamadı: {}", e.getMessage());
        }
        
        // Data usage summary
        BigDecimal dataTotal = categoryTotals.getOrDefault("data", BigDecimal.ZERO);
        if (dataTotal.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("Data kullanımı için ").append(dataTotal).append(" TL ödediniz. ");
        }
        
        // Voice usage summary
        BigDecimal voiceTotal = categoryTotals.getOrDefault("voice", BigDecimal.ZERO);
        if (voiceTotal.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("Ses kullanımı ").append(voiceTotal).append(" TL. ");
        }
        
        // SMS usage summary
        BigDecimal smsTotal = categoryTotals.getOrDefault("sms", BigDecimal.ZERO);
        if (smsTotal.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("SMS kullanımı ").append(smsTotal).append(" TL. ");
        }
        
        // Premium SMS analysis
        BigDecimal premiumSMSTotal = categoryTotals.getOrDefault("premium_sms", BigDecimal.ZERO);
        if (premiumSMSTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalBill = bill.getTotalAmount();
            BigDecimal percentage = premiumSMSTotal.divide(totalBill, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            summary.append("Ücret artışının %").append(percentage.setScale(1, RoundingMode.HALF_UP))
                    .append("'i Premium SMS kaynaklı. ");
        }
        
        // VAS analysis
        BigDecimal vasTotal = categoryTotals.getOrDefault("vas", BigDecimal.ZERO);
        if (vasTotal.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("VAS hizmetleri ").append(vasTotal).append(" TL. ");
        }
        
        // Roaming analysis
        BigDecimal roamingTotal = categoryTotals.getOrDefault("roaming", BigDecimal.ZERO);
        if (roamingTotal.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("Yurt dışı kullanım ").append(roamingTotal).append(" TL. ");
        }
        
        // Savings hint
        String savingsHint = calculateSavingsHint(billItemRepository.findByBill_BillId(bill.getBillId()));
        if (!savingsHint.equals("Faturanızda tasarruf fırsatı bulunmuyor")) {
            summary.append("Tasarruf fırsatı: ").append(savingsHint);
        }
        
        return summary.toString();
    }

    private String calculateSavingsHint(List<BillItem> items) {
        BigDecimal totalSavings = BigDecimal.ZERO;
        
        // Premium SMS savings
        BigDecimal premiumSMSTotal = items.stream()
                .filter(item -> ItemCategory.PREMIUM_SMS.equals(item.getCategory()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (premiumSMSTotal.compareTo(BigDecimal.ZERO) > 0) {
            totalSavings = totalSavings.add(premiumSMSTotal);
        }
        
        // VAS savings (excluding plan fee)
        BigDecimal vasTotal = items.stream()
                .filter(item -> ItemCategory.VAS.equals(item.getCategory()) && !"plan_fee".equals(item.getSubtype()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (vasTotal.compareTo(BigDecimal.ZERO) > 0) {
            totalSavings = totalSavings.add(vasTotal);
        }
        
        // Data overage savings (if significant)
        BigDecimal dataOverageTotal = items.stream()
                .filter(item -> ItemCategory.DATA.equals(item.getCategory()) && "data_overage".equals(item.getSubtype()))
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (dataOverageTotal.compareTo(new BigDecimal("50")) > 0) {
            totalSavings = totalSavings.add(dataOverageTotal.multiply(new BigDecimal("0.3"))); // Assume 30% savings potential
        }
        
        if (totalSavings.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("Bu ay %s TL tasarruf edebilirsiniz", totalSavings.setScale(2, RoundingMode.HALF_UP));
        } else {
            return "Faturanızda tasarruf fırsatı bulunmuyor";
        }
    }

    /**
     * Bill item için tarih bilgisini alır
     * Eğer created_at tarihi varsa, "07.06" formatında döner
     */
    private String getDateInfoForItem(BillItem item) {
        if (item.getCreatedAt() != null) {
            return item.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM"));
        }
        return null;
    }
}
