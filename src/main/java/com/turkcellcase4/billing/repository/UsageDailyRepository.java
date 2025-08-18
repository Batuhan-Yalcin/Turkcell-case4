package com.turkcellcase4.billing.repository;

import com.turkcellcase4.billing.model.UsageDaily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageDailyRepository extends JpaRepository<UsageDaily, Long> {
    
    /**
     * Kullanıcının belirli tarih aralığındaki günlük kullanım verilerini getirir
     */
    List<UsageDaily> findByUser_UserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Kullanıcının belirli tarihten itibaren günlük kullanım verilerini getirir
     */
    List<UsageDaily> findByUser_UserIdAndDateGreaterThanEqual(Long userId, LocalDate startDate);
    
    /**
     * Kullanıcının ortalama data kullanımını hesaplar
     */
    @Query("SELECT AVG(ud.mbUsed) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Double getAverageDataUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    /**
     * Kullanıcının ortalama ses kullanımını hesaplar
     */
    @Query("SELECT AVG(ud.minutesUsed) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Double getAverageVoiceUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    /**
     * Kullanıcının ortalama SMS kullanımını hesaplar
     */
    @Query("SELECT AVG(ud.smsUsed) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Double getAverageSMSUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    /**
     * Kullanıcının toplam roaming kullanımını hesaplar
     */
    @Query("SELECT SUM(ud.roamingMb) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Double getTotalRoamingUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    /**
     * Kullanıcının aylık toplam data kullanımını hesaplar
     */
    @Query("SELECT SUM(ud.mbUsed) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Double getTotalDataUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    /**
     * Kullanıcının aylık toplam ses kullanımını hesaplar
     */
    @Query("SELECT SUM(ud.minutesUsed) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Integer getTotalVoiceUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
    
    /**
     * Kullanıcının aylık toplam SMS kullanımını hesaplar
     */
    @Query("SELECT SUM(ud.smsUsed) FROM UsageDaily ud WHERE ud.user.userId = :userId AND ud.date >= :startDate")
    Integer getTotalSMSUsage(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
