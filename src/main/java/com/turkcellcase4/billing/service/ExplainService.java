package com.turkcellcase4.billing.service;

import com.turkcellcase4.billing.dto.ExplainRequestDTO;
import com.turkcellcase4.billing.dto.ExplainResponseDTO;
import com.turkcellcase4.billing.dto.BillSummaryDTO;
import com.turkcellcase4.billing.dto.CategoryBreakdownDTO;

import java.util.List;

public interface ExplainService {
    
    ExplainResponseDTO explainBill(ExplainRequestDTO request);
    
    BillSummaryDTO getBillSummary(Long billId);
    
    List<CategoryBreakdownDTO> getCategoryBreakdowns(Long billId);
    
    CategoryBreakdownDTO getCategoryBreakdown(Long billId, String category);
}
