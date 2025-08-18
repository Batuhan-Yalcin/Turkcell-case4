package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.dto.BillItemDTO;

import java.time.LocalDate;
import java.util.List;

public interface BillService {
    
    BillResponseDTO getBillById(Long billId);
    
    BillResponseDTO getBillByUserIdAndPeriod(Long userId, String period);
    
    List<BillResponseDTO> getRecentBillsByUserId(Long userId);
    
    List<BillItemDTO> getBillItemsByBillId(Long billId);
    
    List<BillResponseDTO> getBillsByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}
