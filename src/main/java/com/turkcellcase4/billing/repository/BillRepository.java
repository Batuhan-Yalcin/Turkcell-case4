package com.turkcellcase4.billing.repository;

import com.turkcellcase4.billing.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
	
	List<Bill> findByUser_UserIdAndPeriodStartBetween(Long userId, LocalDate start, LocalDate end);
	
	@Query("SELECT b FROM Bill b LEFT JOIN FETCH b.user WHERE b.user.userId = :userId AND b.periodStart >= :startDate")
	List<Bill> findRecentBillsByUserId(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
	
	@Query("SELECT b FROM Bill b LEFT JOIN FETCH b.user WHERE b.user.userId = :userId AND EXTRACT(YEAR FROM b.periodStart) = :year AND EXTRACT(MONTH FROM b.periodStart) = :month")
	Optional<Bill> findByUserIdAndPeriod(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
	
	@Query("SELECT b FROM Bill b LEFT JOIN FETCH b.user WHERE b.user.userId = :userId ORDER BY b.periodStart DESC")
	List<Bill> findAllByUserIdOrderByPeriodStartDesc(@Param("userId") Long userId);
	
	// N+1 sorgu problemlerini çözmek için batch query'ler
	@Query("SELECT b FROM Bill b LEFT JOIN FETCH b.billItems WHERE b.billId IN :billIds")
	List<Bill> findByIdsWithBillItems(@Param("billIds") List<Long> billIds);
	
	@Query("SELECT b FROM Bill b LEFT JOIN FETCH b.user WHERE b.billId IN :billIds")
	List<Bill> findByIdsWithUser(@Param("billIds") List<Long> billIds);
	
	@Query("SELECT b FROM Bill b LEFT JOIN FETCH b.billItems LEFT JOIN FETCH b.user WHERE b.billId IN :billIds")
	List<Bill> findByIdsWithBillItemsAndUser(@Param("billIds") List<Long> billIds);
}
