package com.turkcellcase4.catalog.model;

import com.turkcellcase4.common.BaseEntity;
import com.turkcellcase4.common.enums.PlanType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;
    
    @Column(name = "plan_name", nullable = false)
    private String planName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType type;
    
    @Column(name = "quota_gb")
    private Double quotaGb;
    
    @Column(name = "quota_min")
    private Integer quotaMin;
    
    @Column(name = "quota_sms")
    private Integer quotaSms;
    
    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;
    
    @Column(name = "overage_gb")
    private BigDecimal overageGb;
    
    @Column(name = "overage_min")
    private BigDecimal overageMin;
    
    @Column(name = "overage_sms")
    private BigDecimal overageSms;
}
