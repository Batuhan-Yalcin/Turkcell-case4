package com.turkcellcase4.catalog.repository;

import com.turkcellcase4.catalog.model.VAS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VASRepository extends JpaRepository<VAS, Long> {
    List<VAS> findByProvider(String provider);

    @Query("SELECT v FROM VAS v WHERE v.monthlyFee <= :maxFee")
    List<VAS> findByMaxMonthlyFee(@Param("maxFee") BigDecimal maxFee);
}
