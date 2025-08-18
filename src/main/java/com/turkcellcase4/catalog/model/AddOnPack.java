package com.turkcellcase4.catalog.model;

import com.turkcellcase4.common.BaseEntity;
import com.turkcellcase4.common.enums.AddOnType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "add_on_packs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddOnPack extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "addon_id")
    private Long addonId;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddOnType type;
    
    @Column(name = "extra_gb")
    private Double extraGb;
    
    @Column(name = "extra_min")
    private Integer extraMin;
    
    @Column(name = "extra_sms")
    private Integer extraSms;
    
    @Column(nullable = false)
    private BigDecimal price;
}
