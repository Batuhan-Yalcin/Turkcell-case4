package com.turkcellcase4.billing.model;

import com.turkcellcase4.common.BaseEntity;
import com.turkcellcase4.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Entity
@Table(name = "usage_daily")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageDaily extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(name = "mb_used")
    private Double mbUsed;
    
    @Column(name = "minutes_used")
    private Integer minutesUsed;
    
    @Column(name = "sms_used")
    private Integer smsUsed;
    
    @Column(name = "roaming_mb")
    private Double roamingMb;
}
