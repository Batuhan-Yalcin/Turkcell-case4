package com.turkcellcase4.starter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcellcase4.billing.controller.AnomalyController;
import com.turkcellcase4.billing.dto.AnomalyRequestDTO;
import com.turkcellcase4.billing.dto.AnomalyResponseDTO;
import com.turkcellcase4.billing.dto.AnomalyDTO;
import com.turkcellcase4.billing.service.AnomalyService;
import com.turkcellcase4.common.enums.AnomalyType;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnomalyControllerTest {

    @Mock
    private AnomalyService anomalyService;

    @InjectMocks
    private AnomalyController anomalyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(anomalyController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void detectAnomalies_ShouldReturnAnomalyResponse() throws Exception {
        // Given
        AnomalyRequestDTO request = AnomalyRequestDTO.builder()
                .userId(1001L)
                .period("2025-02")
                .build();

        List<AnomalyDTO> anomalies = Arrays.asList(
                AnomalyDTO.builder()
                        .type(AnomalyType.SPIKE)
                        .category("DATA")
                        .subtype("data_spike")
                        .delta(new BigDecimal("25.00"))
                        .percentageChange(new BigDecimal("180"))
                        .reason("Data kullanımında ani artış")
                        .suggestedAction("Daha büyük plana geçin")
                        .build()
        );

        AnomalyResponseDTO response = AnomalyResponseDTO.builder()
                .userId(1001L)
                .period("2025-02")
                .anomalies(anomalies)
                .totalAnomalies(1)
                .build();

        when(anomalyService.detectAnomalies(any(AnomalyRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/anomalies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1001))
                .andExpect(jsonPath("$.period").value("2025-02"))
                .andExpect(jsonPath("$.totalAnomalies").value(1))
                .andExpect(jsonPath("$.anomalies[0].category").value("DATA"))
                .andExpect(jsonPath("$.anomalies[0].type").value("SPIKE"));

        verify(anomalyService).detectAnomalies(any(AnomalyRequestDTO.class));
    }

    @Test
    void detectAnomalies_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        AnomalyRequestDTO request = AnomalyRequestDTO.builder()
                .userId(null)
                .period("")
                .build();

        // When & Then
        mockMvc.perform(post("/anomalies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAnomalyHistory_ShouldReturnHistoricalAnomalies() throws Exception {
        // Given
        Long userId = 1001L;
        List<AnomalyDTO> anomalies = Arrays.asList(
                AnomalyDTO.builder()
                        .type(AnomalyType.SPIKE)
                        .category("VOICE")
                        .subtype("voice_spike")
                        .delta(new BigDecimal("15.00"))
                        .percentageChange(new BigDecimal("120"))
                        .reason("Ses kullanımında artış")
                        .suggestedAction("Ses paketi ekleyin")
                        .build()
        );

        AnomalyResponseDTO response = AnomalyResponseDTO.builder()
                .userId(userId)
                .period("2025-02")
                .anomalies(anomalies)
                .totalAnomalies(1)
                .build();

        when(anomalyService.getAnomalyHistory(userId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/anomalies/history/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalAnomalies").value(1))
                .andExpect(jsonPath("$.anomalies[0].category").value("VOICE"));

        verify(anomalyService).getAnomalyHistory(userId);
    }

    @Test
    void getAnomalyHistory_WithNonExistentUserId_ShouldReturnEmptyResponse() throws Exception {
        // Given
        Long userId = 999999L;
        AnomalyResponseDTO response = AnomalyResponseDTO.builder()
                .userId(userId)
                .period("2025-02")
                .anomalies(Arrays.asList())
                .totalAnomalies(0)
                .build();

        when(anomalyService.getAnomalyHistory(userId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/anomalies/history/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnomalies").value(0))
                .andExpect(jsonPath("$.anomalies").isEmpty());

        verify(anomalyService).getAnomalyHistory(userId);
    }

    @Test
    void getAnomalySummary_ShouldReturnAnomalySummary() throws Exception {
        // Given
        Long userId = 1001L;
        List<AnomalyDTO> anomalies = Arrays.asList(
                AnomalyDTO.builder()
                        .type(AnomalyType.NEW_ITEM)
                        .category("ROAMING")
                        .subtype("roaming_new")
                        .delta(new BigDecimal("20.00"))
                        .percentageChange(new BigDecimal("200"))
                        .reason("Yeni roaming hizmeti")
                        .suggestedAction("Roaming paketi ekleyin")
                        .build()
        );

        AnomalyResponseDTO response = AnomalyResponseDTO.builder()
                .userId(userId)
                .period("2025-02")
                .anomalies(anomalies)
                .totalAnomalies(1)
                .build();

        when(anomalyService.getAnomalySummary(userId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/anomalies/summary/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalAnomalies").value(1))
                .andExpect(jsonPath("$.anomalies[0].category").value("ROAMING"))
                .andExpect(jsonPath("$.anomalies[0].type").value("NEW_ITEM"));

        verify(anomalyService).getAnomalySummary(userId);
    }

    @Test
    void detectAnomalies_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Given
        AnomalyRequestDTO request = AnomalyRequestDTO.builder()
                .userId(1001L)
                .period("2025-02")
                .build();

        when(anomalyService.detectAnomalies(any(AnomalyRequestDTO.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/anomalies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(anomalyService).detectAnomalies(any(AnomalyRequestDTO.class));
    }

    @Test
    void detectAnomalies_WithMultipleAnomalies_ShouldReturnAllAnomalies() throws Exception {
        // Given
        AnomalyRequestDTO request = AnomalyRequestDTO.builder()
                .userId(1001L)
                .period("2025-02")
                .build();

        List<AnomalyDTO> anomalies = Arrays.asList(
                AnomalyDTO.builder()
                        .type(AnomalyType.SPIKE)
                        .category("DATA")
                        .subtype("data_spike")
                        .delta(new BigDecimal("25.00"))
                        .percentageChange(new BigDecimal("180"))
                        .reason("Data kullanımında ani artış")
                        .suggestedAction("Daha büyük plana geçin")
                        .build(),
                AnomalyDTO.builder()
                        .type(AnomalyType.SPIKE)
                        .category("VOICE")
                        .subtype("voice_drop")
                        .delta(new BigDecimal("-10.00"))
                        .percentageChange(new BigDecimal("-30"))
                        .reason("Ses kullanımında düşüş")
                        .suggestedAction("Planı küçültün")
                        .build()
        );

        AnomalyResponseDTO response = AnomalyResponseDTO.builder()
                .userId(1001L)
                .period("2025-02")
                .anomalies(anomalies)
                .totalAnomalies(2)
                .build();

        when(anomalyService.detectAnomalies(any(AnomalyRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/anomalies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAnomalies").value(2))
                .andExpect(jsonPath("$.anomalies").isArray())
                .andExpect(jsonPath("$.anomalies.length()").value(2))
                .andExpect(jsonPath("$.anomalies[0].type").value("SPIKE"))
                .andExpect(jsonPath("$.anomalies[1].type").value("DROP"));

        verify(anomalyService).detectAnomalies(any(AnomalyRequestDTO.class));
    }

    @Test
    void getAnomalyHistory_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidUserId = "invalid";

        // When & Then
        mockMvc.perform(get("/anomalies/history/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAnomalySummary_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidUserId = "invalid";

        // When & Then
        mockMvc.perform(get("/anomalies/summary/{userId}", invalidUserId))
                .andExpect(status().isBadRequest());
    }
}
