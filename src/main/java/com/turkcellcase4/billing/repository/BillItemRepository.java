package com.turkcellcase4.billing.repository;

import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.common.enums.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
	
	// N+1 sorgu problemlerini çözmek için batch query'ler
	@Query("SELECT bi FROM BillItem bi WHERE bi.bill.billId IN :billIds")
	List<BillItem> findByBillIdsIn(@Param("billIds") List<Long> billIds);
	
	@Query("SELECT bi FROM BillItem bi WHERE bi.bill.billId IN :billIds AND bi.category = :category")
	List<BillItem> findByBillIdsInAndCategory(@Param("billIds") List<Long> billIds, @Param("category") ItemCategory category);
	
	@Query("SELECT bi.bill.billId, bi FROM BillItem bi WHERE bi.bill.billId IN :billIds")
	List<Object[]> findBillItemsGroupedByBillId(@Param("billIds") List<Long> billIds);
	
	// Kategori bazında toplam hesaplama için
	@Query("SELECT bi.bill.billId, bi.category, SUM(bi.amount) FROM BillItem bi WHERE bi.bill.billId IN :billIds GROUP BY bi.bill.billId, bi.category")
	List<Object[]> getCategoryTotalsByBillIds(@Param("billIds") List<Long> billIds);
}
