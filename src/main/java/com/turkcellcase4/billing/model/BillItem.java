package com.turkcellcase4.billing.model;

import com.turkcellcase4.common.BaseEntity;
import com.turkcellcase4.common.enums.ItemCategory;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_items")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillItem extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;
    
    @Column(nullable = false)
    private String subtype;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "tax_rate")
    private BigDecimal taxRate;
}
