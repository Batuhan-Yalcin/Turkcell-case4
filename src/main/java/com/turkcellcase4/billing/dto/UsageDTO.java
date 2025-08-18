package com.turkcellcase4.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageDTO {
    
    private Long id;
    private Long userId;
    private LocalDate date;
    private Double mbUsed;
    private Integer minutesUsed;
    private Integer smsUsed;
    private Double roamingMb;
    
    // Hesaplanan alanlar
    private Double gbUsed; // MB'ı GB'a çevir
    private String formattedDate; // Tarihi formatla
}
