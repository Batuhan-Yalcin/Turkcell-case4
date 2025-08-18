package com.turkcellcase4.catalog.model;

import com.turkcellcase4.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "premium_sms_catalog")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumSMS extends BaseEntity {
    
    @Id
    @Column(name = "shortcode", nullable = false)
    private String shortcode;
    
    @Column(nullable = false)
    private String provider;
    
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;
}
