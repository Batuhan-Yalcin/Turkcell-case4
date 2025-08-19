package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.dto.BillItemDTO;
import com.turkcellcase4.billing.dto.BillSummaryDTO;
import com.turkcellcase4.billing.dto.CreateBillRequestDTO;
import com.turkcellcase4.billing.mapper.BillMapper;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.model.BillItem;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.repository.BillItemRepository;
import com.turkcellcase4.billing.service.BillService;
import com.turkcellcase4.common.enums.ItemCategory;
import com.turkcellcase4.common.exception.ResourceNotFoundException;
import com.turkcellcase4.common.exception.BusinessLogicException;
import com.turkcellcase4.user.repository.UserRepository;
import com.turkcellcase4.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillServiceImpl implements BillService {

	private final BillRepository billRepository;
	private final BillItemRepository billItemRepository;
	private final BillMapper billMapper;
	private final UserRepository userRepository;

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
      LocalDate startDate = LocalDate.now().minusYears(2); // Son 2 yıla genişlet
      List<Bill> bills = billRepository.findRecentBillsByUserId(userId, startDate);
      return billMapper.toBillResponseDTOList(bills);
    } catch (Exception e) {
      throw new BusinessLogicException("Son faturalar getirme hatası: " + e.getMessage());
    }
  }

	@Override
	public List<String> getAvailablePeriods(Long userId) {
		log.info("Getting available periods for user: {}", userId);
		try {
			List<Bill> bills = billRepository.findAllByUserIdOrderByPeriodStartDesc(userId);
			return bills.stream()
					.map(bill -> bill.getPeriodStart().format(DateTimeFormatter.ofPattern("yyyy-MM")))
					.distinct()
					.toList();
		} catch (Exception e) {
			throw new BusinessLogicException("Dönem listesi getirme hatası: " + e.getMessage());
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
	public BillSummaryDTO getBillSummary(Long billId) {
		log.info("Getting bill summary for bill: {}", billId);
		try {
			Bill bill = billRepository.findById(billId)
					.orElseThrow(() -> new ResourceNotFoundException("Fatura bulunamadı: " + billId));
			
			List<BillItem> items = billItemRepository.findByBill_BillId(billId);
			
			BigDecimal totalAmount = bill.getTotalAmount();
			BigDecimal taxes = items.stream()
					.filter(item -> ItemCategory.TAX.equals(item.getCategory()))
					.map(BillItem::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			BigDecimal usageBasedCharges = items.stream()
					.filter(item -> ItemCategory.DATA.equals(item.getCategory()) || 
								   ItemCategory.VOICE.equals(item.getCategory()) || 
								   ItemCategory.SMS.equals(item.getCategory()) ||
								   ItemCategory.ROAMING.equals(item.getCategory()))
					.map(BillItem::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			BigDecimal oneTimeCharges = items.stream()
					.filter(item -> ItemCategory.ONE_OFF.equals(item.getCategory()))
					.map(BillItem::getAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			
			String savingsHint = calculateSavingsHint(items);
			
			return BillSummaryDTO.builder()
					.totalAmount(totalAmount)
					.taxes(taxes)
					.usageBasedCharges(usageBasedCharges)
					.oneTimeCharges(oneTimeCharges)
					.savingsHint(savingsHint)
					.build();
		} catch (Exception e) {
			if (e instanceof ResourceNotFoundException) {
				throw e;
			}
			throw new BusinessLogicException("Fatura özeti oluşturma hatası: " + e.getMessage());
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

	@Override
	public BillResponseDTO createBill(CreateBillRequestDTO request) {
		log.info("Creating bill for user: {} and period: {} to {}", request.getUserId(), request.getPeriodStart(), request.getPeriodEnd());
		
		try {
			// Kullanıcı entity'sini al
			User user = userRepository.findById(request.getUserId())
					.orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + request.getUserId()));
			
			// Yeni fatura oluştur
			Bill bill = new Bill();
			bill.setPeriodStart(request.getPeriodStart());
			bill.setPeriodEnd(request.getPeriodEnd());
			bill.setIssueDate(request.getIssueDate());
			bill.setTotalAmount(request.getTotalAmount());
			bill.setCurrency(request.getCurrency());
			
			// Kullanıcıyı set et
			bill.setUser(user);
			
			// Faturayı kaydet
			Bill savedBill = billRepository.save(bill);
			
			// Fatura kalemlerini oluştur ve kaydet
			List<BillItem> billItems = request.getBillItems().stream()
					.map(itemRequest -> {
						BillItem item = new BillItem();
						item.setBill(savedBill);
						item.setCategory(ItemCategory.valueOf(itemRequest.getCategory().toUpperCase()));
						item.setSubtype(itemRequest.getSubtype());
						item.setDescription(itemRequest.getDescription());
						item.setAmount(itemRequest.getAmount());
						item.setUnitPrice(itemRequest.getUnitPrice());
						item.setQuantity(itemRequest.getQuantity());
						item.setTaxRate(itemRequest.getTaxRate() != null ? itemRequest.getTaxRate() : BigDecimal.ZERO);
						return item;
					})
					.collect(Collectors.toList());
			
			billItemRepository.saveAll(billItems);
			savedBill.setBillItems(billItems);
			
			return billMapper.toBillResponseDTO(savedBill);
		} catch (Exception e) {
			throw new BusinessLogicException("Fatura oluşturma hatası: " + e.getMessage());
		}
	}

	private String calculateSavingsHint(List<BillItem> items) {
		BigDecimal premiumSMSTotal = items.stream()
				.filter(item -> ItemCategory.PREMIUM_SMS.equals(item.getCategory()))
				.map(BillItem::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal vasTotal = items.stream()
				.filter(item -> ItemCategory.VAS.equals(item.getCategory()))
				.map(BillItem::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		BigDecimal roamingTotal = items.stream()
				.filter(item -> ItemCategory.ROAMING.equals(item.getCategory()))
				.map(BillItem::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		
		if (premiumSMSTotal.compareTo(BigDecimal.ZERO) > 0) {
			return "Premium SMS'i engelleyerek " + premiumSMSTotal + " TL tasarruf edebilirsiniz";
		}
		
		if (vasTotal.compareTo(BigDecimal.ZERO) > 0) {
			return "Kullanmadığınız VAS servislerini kapatarak " + vasTotal + " TL tasarruf edebilirsiniz";
		}
		
		if (roamingTotal.compareTo(BigDecimal.ZERO) > 0) {
			return "Yurt dışı kullanımı sınırlayarak " + roamingTotal + " TL tasarruf edebilirsiniz";
		}
		
		return "Faturanızda tasarruf fırsatı bulunmuyor";
	}
}
