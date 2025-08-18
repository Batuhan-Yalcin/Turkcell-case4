package com.turkcellcase4.catalog.repository;

import com.turkcellcase4.catalog.model.Plan;
import com.turkcellcase4.common.enums.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByType(PlanType type);

    @Query("SELECT p FROM Plan p WHERE p.monthlyPrice BETWEEN :minPrice AND :maxPrice")
    List<Plan> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
}
