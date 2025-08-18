package com.turkcellcase4.catalog.repository;

import com.turkcellcase4.catalog.model.AddOnPack;
import com.turkcellcase4.common.enums.AddOnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface AddOnPackRepository extends JpaRepository<AddOnPack, Long> {
    List<AddOnPack> findByType(AddOnType type);

    @Query("SELECT a FROM AddOnPack a WHERE a.price <= :maxPrice")
    List<AddOnPack> findByMaxPrice(@Param("maxPrice") BigDecimal maxPrice);
}
