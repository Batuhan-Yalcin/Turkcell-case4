package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.dto.AnomalyDTO;
import com.turkcellcase4.billing.service.LLMExplanationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMExplanationServiceImpl implements LLMExplanationService {

    private final WebClient webClient;
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Override
    public String generateAnomalyExplanation(AnomalyDTO anomaly, String userContext) {
        try {
            String prompt = buildAnomalyPrompt(anomaly, userContext);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Anomali açıklaması üretilirken hata: {}", e.getMessage());
            return generateFallbackAnomalyExplanation(anomaly);
        }
    }

    @Override
    public String generateCohortAnalysis(Long userId, String period, Double userAverage, Double cohortAverage) {
        try {
            String prompt = buildCohortPrompt(userId, period, userAverage, cohortAverage);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Kohort analizi üretilirken hata: {}", e.getMessage());
            return generateFallbackCohortAnalysis(userAverage, cohortAverage);
        }
    }

    @Override
    public String generateTaxBreakdownAnalysis(Long billId, Double totalTax, Double effectiveTaxRate) {
        try {
            String prompt = buildTaxPrompt(billId, totalTax, effectiveTaxRate);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Vergi analizi üretilirken hata: {}", e.getMessage());
            return generateFallbackTaxAnalysis(totalTax, effectiveTaxRate);
        }
    }

    @Override
    public String generateAutofixRecommendation(Long userId, String period, Double currentCost, Double potentialSavings) {
        try {
            String prompt = buildAutofixPrompt(userId, period, currentCost, potentialSavings);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Autofix önerisi üretilirken hata: {}", e.getMessage());
            return generateFallbackAutofixRecommendation(currentCost, potentialSavings);
        }
    }

    @Override
    public String generateBillAnalysisSummary(Long billId, String period, Double totalAmount, String mainCategories) {
        try {
            String prompt = buildBillSummaryPrompt(billId, period, totalAmount, mainCategories);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            log.error("Fatura özeti üretilirken hata: {}", e.getMessage());
            return generateFallbackBillSummary(totalAmount, mainCategories);
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            
            part.put("text", prompt);
            content.put("parts", new Object[]{part});
            requestBody.put("contents", new Object[]{content});
            
            // Gemini API çağrısı
            String response = webClient.post()
                    .uri(geminiApiUrl + "?key=" + geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            return parseGeminiResponse(response);
        } catch (Exception e) {
            log.error("Gemini API çağrısında hata: {}", e.getMessage());
            throw new RuntimeException("AI servisi şu anda kullanılamıyor", e);
        }
    }

    private String buildAnomalyPrompt(AnomalyDTO anomaly, String userContext) {
        return String.format("""
            Sen bir Turkcell fatura analisti. Aşağıdaki anomali için 2-3 cümlelik Türkçe açıklama üret:
            
            Anomali Tipi: %s
            Kategori: %s
            Alt Tip: %s
            Delta: %s TL
            Yüzde Değişim: %s%%
            Sebep: %s
            Önerilen Aksiyon: %s
            Kullanıcı Bağlamı: %s
            
            Açıklama: Bu anomali neden oluştu ve ne anlama geliyor? Müşteri için anlaşılır ol.
            """,
            anomaly.getType(),
            anomaly.getCategory(),
            anomaly.getSubtype() != null ? anomaly.getSubtype() : "N/A",
            anomaly.getDelta(),
            anomaly.getPercentageChange(),
            anomaly.getReason(),
            anomaly.getSuggestedAction(),
            userContext
        );
    }

    private String buildCohortPrompt(Long userId, String period, Double userAverage, Double cohortAverage) {
        return String.format("""
            Sen bir Turkcell analisti. Aşağıdaki kohort kıyası için 2-3 cümlelik Türkçe analiz üret:
            
            Kullanıcı ID: %d
            Dönem: %s
            Kullanıcı Ortalaması: %.2f TL
            Kohort Ortalaması: %.2f TL
            Fark: %.2f TL
            
            Analiz: Bu kullanıcı benzer kullanıcılara göre nasıl? Neden fark var?
            """,
            userId, period, userAverage, cohortAverage, userAverage - cohortAverage
        );
    }

    private String buildTaxPrompt(Long billId, Double totalTax, Double effectiveTaxRate) {
        return String.format("""
            Sen bir vergi uzmanı. Aşağıdaki fatura vergi analizi için 2-3 cümlelik Türkçe açıklama üret:
            
            Fatura ID: %d
            Toplam Vergi: %.2f TL
            Efektif Vergi Oranı: %.2f%%
            
            Analiz: Bu vergi tutarı normal mi? Hangi vergi türleri dahil? Müşteri için açıkla.
            """,
            billId, totalTax, effectiveTaxRate * 100
        );
    }

    private String buildAutofixPrompt(Long userId, String period, Double currentCost, Double potentialSavings) {
        return String.format("""
            Sen bir Turkcell tasarruf uzmanı. Aşağıdaki autofix önerisi için 2-3 cümlelik Türkçe açıklama üret:
            
            Kullanıcı ID: %d
            Dönem: %s
            Mevcut Maliyet: %.2f TL
            Potansiyel Tasarruf: %.2f TL
            
            Öneri: Bu müşteri nasıl tasarruf edebilir? Hangi değişiklikleri yapmalı? Gerekçe ile açıkla.
            """,
            userId, period, currentCost, potentialSavings
        );
    }

    private String buildBillSummaryPrompt(Long billId, String period, Double totalAmount, String mainCategories) {
        return String.format("""
            Sen bir Turkcell fatura uzmanı. Aşağıdaki fatura için 2-3 cümlelik Türkçe özet üret:
            
            Fatura ID: %d
            Dönem: %s
            Toplam Tutar: %.2f TL
            Ana Kategoriler: %s
            
            Özet: Bu fatura neden bu kadar yüksek/düşük? Hangi kategoriler öne çıkıyor? Müşteri için açıkla.
            """,
            billId, period, totalAmount, mainCategories
        );
    }

    private String parseGeminiResponse(String response) {
        try {
            // Gemini API response'unu parse et
            if (response != null && response.contains("text")) {
                // JSON response'dan text'i çıkar - daha güvenli parsing
                int textStart = response.indexOf("\"text\":\"");
                if (textStart != -1) {
                    textStart += 9; // "text":" uzunluğu
                    int textEnd = response.indexOf("\"", textStart);
                    if (textEnd != -1) {
                        return response.substring(textStart, textEnd);
                    }
                }
                
                // Fallback parsing
                int start = response.indexOf("text") + 7;
                int end = response.indexOf("}", start);
                if (start > 6 && end > start) {
                    return response.substring(start, end);
                }
            }
            return "AI açıklaması üretilemedi.";
        } catch (Exception e) {
            log.error("Gemini response parsing error: {}", e.getMessage());
            return "AI açıklaması parse edilemedi.";
        }
    }

    // Fallback metodları - API çalışmazsa basit açıklamalar üret
    private String generateFallbackAnomalyExplanation(AnomalyDTO anomaly) {
        return String.format("Bu %s anomali, %s kategorisinde %.2f TL artış ile tespit edildi. %s", 
            anomaly.getType(), anomaly.getCategory(), anomaly.getDelta(), anomaly.getReason());
    }

    private String generateFallbackCohortAnalysis(Double userAverage, Double cohortAverage) {
        double difference = userAverage - cohortAverage;
        if (difference > 0) {
            return String.format("Kullanıcınız benzer kullanıcılara göre %.2f TL daha fazla ödüyor. Bu durum yüksek kullanımdan kaynaklanıyor olabilir.", difference);
        } else {
            return String.format("Kullanıcınız benzer kullanıcılara göre %.2f TL daha az ödüyor. Bu durum verimli kullanımdan kaynaklanıyor olabilir.", Math.abs(difference));
        }
    }

    private String generateFallbackTaxAnalysis(Double totalTax, Double effectiveTaxRate) {
        return String.format("Toplam %.2f TL vergi ödenmiş. Efektif vergi oranı %.2f%% olarak hesaplanmış. Bu oran standart KDV oranına yakın.", totalTax, effectiveTaxRate * 100);
    }

    private String generateFallbackAutofixRecommendation(Double currentCost, Double potentialSavings) {
        return String.format("Mevcut maliyet %.2f TL. Potansiyel tasarruf %.2f TL. Plan değişikliği veya ek paket iptali ile tasarruf edebilirsiniz.", currentCost, potentialSavings);
    }

    private String generateFallbackBillSummary(Double totalAmount, String mainCategories) {
        return String.format("Fatura tutarı %.2f TL. Ana kategoriler: %s. Bu tutar normal kullanım için uygun görünüyor.", totalAmount, mainCategories);
    }
}
