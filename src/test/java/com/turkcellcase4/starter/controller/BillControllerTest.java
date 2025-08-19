package com.turkcellcase4.starter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcellcase4.billing.controller.BillController;
import com.turkcellcase4.billing.dto.BillResponseDTO;
import com.turkcellcase4.billing.dto.BillItemDTO;
import com.turkcellcase4.billing.dto.BillSummaryDTO;
import com.turkcellcase4.billing.service.BillService;
import com.turkcellcase4.common.enums.ItemCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BillControllerTest {

    @Mock
    private BillService billService;

    @InjectMocks
    private BillController billController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(billController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getBillById_ShouldReturnBill() throws Exception {
        Long billId = 700101L;
        BillResponseDTO billResponse = BillResponseDTO.builder()
                .billId(billId)
                .userId(1001L)
                .periodStart(LocalDate.of(2025, 2, 1))
                .periodEnd(LocalDate.of(2025, 2, 28))
                .totalAmount(new BigDecimal("85.50"))
                .issueDate(LocalDate.of(2025, 2, 15))
                .dueDate(LocalDate.of(2025, 3, 15))
                .currency("TRY")
                .status("UNPAID")
                .build();

        when(billService.getBillById(billId)).thenReturn(billResponse);

        mockMvc.perform(get("/bills/{billId}", billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.billId").value(billId))
                .andExpect(jsonPath("$.userId").value(1001))
                .andExpect(jsonPath("$.periodStart").value("2025-02-01"))
                .andExpect(jsonPath("$.periodEnd").value("2025-02-28"))
                .andExpect(jsonPath("$.totalAmount").value(85.50))
                .andExpect(jsonPath("$.status").value("UNPAID"));

        verify(billService).getBillById(billId);
    }

    @Test
    void getRecentBillsByUserId_ShouldReturnBillsList() throws Exception {
        Long userId = 1001L;
        List<BillResponseDTO> bills = Arrays.asList(
                BillResponseDTO.builder()
                        .billId(700101L)
                        .userId(userId)
                        .periodStart(LocalDate.of(2025, 2, 1))
                        .periodEnd(LocalDate.of(2025, 2, 28))
                        .totalAmount(new BigDecimal("85.50"))
                        .issueDate(LocalDate.of(2025, 2, 15))
                        .dueDate(LocalDate.of(2025, 3, 15))
                        .currency("TRY")
                        .status("UNPAID")
                        .build(),
                BillResponseDTO.builder()
                        .billId(700102L)
                        .userId(userId)
                        .periodStart(LocalDate.of(2025, 1, 1))
                        .periodEnd(LocalDate.of(2025, 1, 31))
                        .totalAmount(new BigDecimal("72.30"))
                        .issueDate(LocalDate.of(2025, 1, 15))
                        .dueDate(LocalDate.of(2025, 2, 15))
                        .currency("TRY")
                        .status("PAID")
                        .build()
        );

        when(billService.getRecentBillsByUserId(userId)).thenReturn(bills);

        mockMvc.perform(get("/bills/user/{userId}/recent", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].periodStart").value("2025-02-01"))
                .andExpect(jsonPath("$[1].periodStart").value("2025-01-01"))
                .andExpect(jsonPath("$[0].status").value("UNPAID"))
                .andExpect(jsonPath("$[1].status").value("PAID"));

        verify(billService).getRecentBillsByUserId(userId);
    }

    @Test
    void getBillItemsByBillId_ShouldReturnBillItems() throws Exception {
        Long billId = 700101L;
        List<BillItemDTO> billItems = Arrays.asList(
                BillItemDTO.builder()
                        .category(ItemCategory.DATA)
                        .description("Data kullanımı")
                        .amount(new BigDecimal("45.50"))
                        .quantity(1)
                        .unitPrice(new BigDecimal("45.50"))
                        .build(),
                BillItemDTO.builder()
                        .category(ItemCategory.VOICE)
                        .description("Ses kullanımı")
                        .amount(new BigDecimal("25.00"))
                        .quantity(1)
                        .unitPrice(new BigDecimal("25.00"))
                        .build()
        );

        when(billService.getBillItemsByBillId(billId)).thenReturn(billItems);

        mockMvc.perform(get("/bills/{billId}/items", billId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("DATA"))
                .andExpect(jsonPath("$[1].category").value("VOICE"))
                .andExpect(jsonPath("$[0].amount").value(45.50))
                .andExpect(jsonPath("$[1].amount").value(25.00));

        verify(billService).getBillItemsByBillId(billId);
    }
}
