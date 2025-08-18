package com.turkcellcase4.billing.repository;

import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.common.enums.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BillItemRepository extends JpaRepository<BillItem, Long> {
	
	List<BillItem> findByBill_BillId(Long billId);
	
	List<BillItem> findByBill_BillIdAndCategory(Long billId, ItemCategory category);
	
	@Query("SELECT bi FROM BillItem bi WHERE bi.bill.user.userId = :userId AND bi.category = :category AND bi.bill.periodStart >= :startDate")
	List<BillItem> findByUserIdAndCategoryAndPeriod(@Param("userId") Long userId, @Param("category") ItemCategory category, @Param("startDate") LocalDate startDate);
	
	@Query("SELECT bi.category, SUM(bi.amount) FROM BillItem bi WHERE bi.bill.billId = :billId GROUP BY bi.category")
	List<Object[]> getCategoryTotalsByBillId(@Param("billId") Long billId);
	
	@Query("SELECT bi FROM BillItem bi WHERE bi.bill.user.userId = :userId AND bi.bill.periodStart >= :startDate ORDER BY bi.bill.periodStart DESC")
	List<BillItem> findByUserIdAndPeriodOrderByDate(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);
}
