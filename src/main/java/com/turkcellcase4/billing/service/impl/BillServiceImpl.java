package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.dto.BillItemDTO;
import com.turkcellcase4.billing.mapper.BillMapper;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.repository.BillItemRepository;
import com.turkcellcase4.billing.service.BillService;
import com.turkcellcase4.common.exception.ResourceNotFoundException;
import com.turkcellcase4.common.exception.BusinessLogicException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillServiceImpl implements BillService {

	private final BillRepository billRepository;
	private final BillItemRepository billItemRepository;
	private final BillMapper billMapper;

	@Override
	public BillResponseDTO getBillById(Long billId) {
		log.info("Getting bill by ID: {}", billId);
		Bill bill = billRepository.findById(billId)
				.orElseThrow(() -> new ResourceNotFoundException("Fatura bulunamadı: " + billId));
		return billMapper.toBillResponseDTO(bill);
	}

	@Override
	public BillResponseDTO getBillByUserIdAndPeriod(Long userId, String period) {
		log.info("Getting bill for user {} and period: {}", userId, period);
		
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			LocalDate periodDate = LocalDate.parse(period + "-01", formatter);
			
			int year = periodDate.getYear();
			int month = periodDate.getMonthValue();
			
			Bill bill = billRepository.findByUserIdAndPeriod(userId, year, month)
					.orElseThrow(() -> new ResourceNotFoundException("Fatura bulunamadı: kullanıcı " + userId + " ve dönem " + period));
			
			return billMapper.toBillResponseDTO(bill);
		} catch (Exception e) {
			if (e instanceof ResourceNotFoundException) {
				throw e;
			}
			throw new BusinessLogicException("Fatura getirme hatası: " + e.getMessage());
		}
	}

	@Override
	public List<BillResponseDTO> getRecentBillsByUserId(Long userId) {
		log.info("Getting recent bills for user: {}", userId);
		try {
			LocalDate startDate = LocalDate.now().minusMonths(6);
			List<Bill> bills = billRepository.findRecentBillsByUserId(userId, startDate);
			return billMapper.toBillResponseDTOList(bills);
		} catch (Exception e) {
			throw new BusinessLogicException("Son faturalar getirme hatası: " + e.getMessage());
		}
	}

	@Override
	public List<BillItemDTO> getBillItemsByBillId(Long billId) {
		log.info("Getting bill items for bill: {}", billId);
		try {
			List<BillItem> items = billItemRepository.findByBill_BillId(billId);
			return billMapper.toBillItemDTOList(items);
		} catch (Exception e) {
			throw new BusinessLogicException("Fatura kalemleri getirme hatası: " + e.getMessage());
		}
	}

	@Override
	public List<BillResponseDTO> getBillsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
		log.info("Getting bills for user {} between {} and {}", userId, startDate, endDate);
		try {
			List<Bill> bills = billRepository.findByUser_UserIdAndPeriodStartBetween(userId, startDate, endDate);
			return billMapper.toBillResponseDTOList(bills);
		} catch (Exception e) {
			throw new BusinessLogicException("Tarih aralığında faturalar getirme hatası: " + e.getMessage());
		}
	}
}
