package com.turkcellcase4.billing.service.impl;

import com.turkcellcase4.billing.dto.CohortAnalysisDTO;
import com.turkcellcase4.billing.model.Bill;
import com.turkcellcase4.billing.repository.BillRepository;
import com.turkcellcase4.billing.service.CohortService;
import com.turkcellcase4.user.model.User;
import com.turkcellcase4.user.repository.UserRepository;
import com.turkcellcase4.common.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CohortServiceImpl implements CohortService {

    private final BillRepository billRepository;
    private final UserRepository userRepository;

    @Override
    public CohortAnalysisDTO analyzeUserCohort(Long userId, String period) {
        log.info("Analyzing user cohort for userId: {} and period: {}", userId, period);
        
        try {
            // Get user type
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Parse period
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate periodDate = LocalDate.parse(period + "-01", formatter);
            
            // Get user's bills for last 6 months
            LocalDate startDate = periodDate.minusMonths(6);
            List<Bill> userBills = billRepository.findRecentBillsByUserId(userId, startDate);
            
            // Calculate user average
            BigDecimal userAverage = userBills.stream()
                    .map(Bill::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(Math.max(userBills.size(), 1)), 2, RoundingMode.HALF_UP);
            
            // Get cohort average (same user type)
            Double cohortAverage = getCohortAverage(user.getType().name(), period);
            
            // Calculate performance rating
            String performanceRating = evaluateUserPerformance(userId, period);
            
            return CohortAnalysisDTO.builder()
                    .userId(userId)
                    .period(period)
                    .userAverage(userAverage)
                    .cohortAverage(BigDecimal.valueOf(cohortAverage))
                    .performanceRating(performanceRating)
                    .build();
        } catch (Exception e) {
            log.error("Cohort analysis error: {}", e.getMessage());
            return CohortAnalysisDTO.builder()
                    .userId(userId)
                    .period(period)
                    .userAverage(BigDecimal.ZERO)
                    .cohortAverage(BigDecimal.ZERO)
                    .performanceRating("ERROR")
                    .build();
        }
    }

    @Override
    public Double getCohortAverage(String userType, String period) {
        log.info("Getting cohort average for userType: {} and period: {}", userType, period);
        
        try {
            // Parse period
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate periodDate = LocalDate.parse(period + "-01", formatter);
            LocalDate startDate = periodDate.minusMonths(6);
            
            // Get all users of same type
            List<User> cohortUsers = userRepository.findByType(UserType.valueOf(userType));
            
            if (cohortUsers.isEmpty()) {
                return 0.0;
            }
            
            // Calculate average bill amount for cohort
            double totalAmount = 0.0;
            int billCount = 0;
            
            for (User cohortUser : cohortUsers) {
                List<Bill> userBills = billRepository.findRecentBillsByUserId(cohortUser.getUserId(), startDate);
                for (Bill bill : userBills) {
                    totalAmount += bill.getTotalAmount().doubleValue();
                    billCount++;
                }
            }
            
            if (billCount == 0) {
                return 0.0;
            }
            
            return totalAmount / billCount;
        } catch (Exception e) {
            log.error("Cohort average calculation error: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public String evaluateUserPerformance(Long userId, String period) {
        log.info("Evaluating user performance for userId: {} and period: {}", userId, period);
        
        try {
            // Get user's current bill
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate periodDate = LocalDate.parse(period + "-01", formatter);
            int year = periodDate.getYear();
            int month = periodDate.getMonthValue();
            
            Bill currentBill = billRepository.findByUserIdAndPeriod(userId, year, month).orElse(null);
            if (currentBill == null) {
                return "NORMAL";
            }
            
            // Get user's average for last 3 months
            LocalDate startDate = periodDate.minusMonths(3);
            List<Bill> recentBills = billRepository.findRecentBillsByUserId(userId, startDate);
            
            if (recentBills.size() < 2) {
                return "NORMAL";
            }
            
            BigDecimal averageAmount = recentBills.stream()
                    .map(Bill::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(recentBills.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal currentAmount = currentBill.getTotalAmount();
            BigDecimal difference = currentAmount.subtract(averageAmount);
            BigDecimal percentageChange = difference.divide(averageAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            
            if (percentageChange.compareTo(new BigDecimal("50")) > 0) {
                return "HIGH";
            } else if (percentageChange.compareTo(new BigDecimal("-30")) < 0) {
                return "LOW";
            } else {
                return "NORMAL";
            }
        } catch (Exception e) {
            log.error("Performance evaluation error: {}", e.getMessage());
            return "NORMAL";
        }
    }

    @Override
    public CohortAnalysisDTO findSimilarUsers(Long userId, String period) {
        log.info("Finding similar users for userId: {} and period: {}", userId, period);
        
        try {
            // Get user type
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Parse period
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate periodDate = LocalDate.parse(period + "-01", formatter);
            LocalDate startDate = periodDate.minusMonths(3);
            
            // Get user's average
            List<Bill> userBills = billRepository.findRecentBillsByUserId(userId, startDate);
            BigDecimal userAverage = userBills.stream()
                    .map(Bill::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(Math.max(userBills.size(), 1)), 2, RoundingMode.HALF_UP);
            
            // Find users with similar spending patterns (±20%)
            List<User> similarUsers = userRepository.findByType(user.getType());
            double totalSimilarAmount = 0.0;
            int similarUserCount = 0;
            
            for (User similarUser : similarUsers) {
                if (similarUser.getUserId().equals(userId)) continue;
                
                List<Bill> similarUserBills = billRepository.findRecentBillsByUserId(similarUser.getUserId(), startDate);
                if (!similarUserBills.isEmpty()) {
                    BigDecimal similarUserAverage = similarUserBills.stream()
                            .map(Bill::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(similarUserBills.size()), 2, RoundingMode.HALF_UP);
                    
                    // Check if spending is within ±20% range
                    BigDecimal difference = userAverage.subtract(similarUserAverage).abs();
                    BigDecimal percentage = difference.divide(userAverage, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    
                    if (percentage.compareTo(new BigDecimal("20")) <= 0) {
                        totalSimilarAmount += similarUserAverage.doubleValue();
                        similarUserCount++;
                    }
                }
            }
            
            BigDecimal cohortAverage = similarUserCount > 0 ? 
                BigDecimal.valueOf(totalSimilarAmount / similarUserCount) : BigDecimal.ZERO;
            
            return CohortAnalysisDTO.builder()
                    .userId(userId)
                    .period(period)
                    .userAverage(userAverage)
                    .cohortAverage(cohortAverage)
                    .performanceRating("SIMILAR")
                    .build();
        } catch (Exception e) {
            log.error("Similar users finding error: {}", e.getMessage());
            return CohortAnalysisDTO.builder()
                    .userId(userId)
                    .period(period)
                    .userAverage(BigDecimal.ZERO)
                    .cohortAverage(BigDecimal.ZERO)
                    .performanceRating("ERROR")
                    .build();
        }
    }
}
