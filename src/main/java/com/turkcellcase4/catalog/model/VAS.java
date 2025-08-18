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
@Table(name = "vas_catalog")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VAS extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vas_id")
    private Long vasId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "monthly_fee", nullable = false)
    private BigDecimal monthlyFee;
    
    @Column(nullable = false)
    private String provider;
}
