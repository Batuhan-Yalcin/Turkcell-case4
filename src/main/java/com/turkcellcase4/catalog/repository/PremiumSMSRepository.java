package com.turkcellcase4.catalog.repository;

import com.turkcellcase4.catalog.model.PremiumSMS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PremiumSMSRepository extends JpaRepository<PremiumSMS, String> {
}
