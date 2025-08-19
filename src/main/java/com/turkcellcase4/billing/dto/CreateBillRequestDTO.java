package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillRequestDTO {
    
    @NotNull(message = "Kullanıcı ID'si zorunludur")
    private Long userId;
    
    @NotNull(message = "Dönem başlangıç tarihi zorunludur")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate periodStart;
    
    @NotNull(message = "Dönem bitiş tarihi zorunludur")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate periodEnd;
    
    @NotNull(message = "Fatura tarihi zorunludur")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate issueDate;
    
    @NotNull(message = "Toplam tutar zorunludur")
    @DecimalMin(value = "0.0", message = "Toplam tutar 0'dan küçük olamaz")
    private BigDecimal totalAmount;
    
    @NotBlank(message = "Para birimi zorunludur")
    private String currency;
    
    @Valid
    @NotEmpty(message = "En az bir fatura kalemi olmalıdır")
    private List<CreateBillItemRequestDTO> billItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateBillItemRequestDTO {
        
        @NotNull(message = "Kategori zorunludur")
        private String category;
        
        @NotBlank(message = "Alt tip zorunludur")
        private String subtype;
        
        @NotBlank(message = "Açıklama zorunludur")
        private String description;
        
        @NotNull(message = "Tutar zorunludur")
        @DecimalMin(value = "0.0", message = "Tutar 0'dan küçük olamaz")
        private BigDecimal amount;
        
        @NotNull(message = "Birim fiyat zorunludur")
        @DecimalMin(value = "0.0", message = "Birim fiyat 0'dan küçük olamaz")
        private BigDecimal unitPrice;
        
        @NotNull(message = "Miktar zorunludur")
        @Min(value = 1, message = "Miktar en az 1 olmalıdır")
        private Integer quantity;
        
        @DecimalMin(value = "0.0", message = "Vergi oranı 0'dan küçük olamaz")
        @DecimalMax(value = "100.0", message = "Vergi oranı 100'den büyük olamaz")
        private BigDecimal taxRate;
    }
}
