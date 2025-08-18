package com.turkcellcase4.user.model;

import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.common.BaseEntity;
import com.turkcellcase4.common.enums.UserType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "current_plan_id")
    private Long currentPlanId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;
    
    @Column(unique = true, nullable = false)
    private String msisdn;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bill> bills;
}
