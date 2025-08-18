package com.turkcellcase4.user.repository;

import com.turkcellcase4.user.model.User;
import com.turkcellcase4.common.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByMsisdn(String msisdn);
    
    List<User> findByType(UserType type);
    
    @Query("SELECT u FROM User u WHERE u.currentPlanId = :planId")
    List<User> findByCurrentPlanId(@Param("planId") Long planId);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
}
