package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.dto.BillItemDTO;
import com.turkcellcase4.billing.dto.BillSummaryDTO;
import com.turkcellcase4.billing.dto.CreateBillRequestDTO;

import java.time.LocalDate;
import java.util.List;

public interface BillService {
    
    BillResponseDTO getBillById(Long billId);
    
    BillResponseDTO getBillByUserIdAndPeriod(Long userId, String period);
    
    List<BillResponseDTO> getRecentBillsByUserId(Long userId);
    
    List<String> getAvailablePeriods(Long userId);
    
    List<BillItemDTO> getBillItemsByBillId(Long billId);
    
    BillSummaryDTO getBillSummary(Long billId);
    
    List<BillResponseDTO> getBillsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    BillResponseDTO createBill(CreateBillRequestDTO request);
}
