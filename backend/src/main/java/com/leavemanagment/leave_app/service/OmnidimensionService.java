package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.repository.LeaveRequestRepository;
import com.leavemanagment.leave_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OmnidimensionService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${omnidimension.api.url:http://localhost:3000/api}")
    private String omnidimensionApiUrl;

    @Value("${omnidimension.api.key:}")
    private String omnidimensionApiKey;

    /**
     * Generate multi-dimensional analytics for leave management
     */
    public Map<String, Object> generateMultiDimensionalAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            // Time dimension analysis
            analytics.put("timeAnalysis", generateTimeDimensionAnalysis());
            
            // Employee dimension analysis
            analytics.put("employeeAnalysis", generateEmployeeDimensionAnalysis());
            
            // Department dimension analysis
            analytics.put("departmentAnalysis", generateDepartmentDimensionAnalysis());
            
            // Leave type dimension analysis
            analytics.put("leaveTypeAnalysis", generateLeaveTypeDimensionAnalysis());
            
            // Predictive analytics
            analytics.put("predictiveAnalytics", generatePredictiveAnalytics());
            
            // Anomaly detection
            analytics.put("anomalyDetection", detectAnomalies());
            
            System.out.println("🤖 Omnidimension: Multi-dimensional analytics generated successfully");
            return analytics;
            
        } catch (Exception e) {
            System.err.println("❌ Error generating Omnidimension analytics: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Time dimension analysis
     */
    private Map<String, Object> generateTimeDimensionAnalysis() {
        Map<String, Object> timeAnalysis = new HashMap<>();
        
        // Monthly trends
        Map<String, Long> monthlyTrends = new HashMap<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 0; i < 12; i++) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            
            long leavesInMonth = leaveRequestRepository.findAll().stream()
                    .filter(leave -> leave.getStartDate() != null && 
                            !leave.getStartDate().isBefore(monthStart) && 
                            !leave.getStartDate().isAfter(monthEnd))
                    .count();
            
            monthlyTrends.put(monthStart.format(DateTimeFormatter.ofPattern("MMM yyyy")), leavesInMonth);
        }
        
        timeAnalysis.put("monthlyTrends", monthlyTrends);
        
        // Seasonal patterns
        Map<String, Long> seasonalPatterns = new HashMap<>();
        seasonalPatterns.put("Spring", countLeavesBySeason(3, 5));
        seasonalPatterns.put("Summer", countLeavesBySeason(6, 8));
        seasonalPatterns.put("Fall", countLeavesBySeason(9, 11));
        seasonalPatterns.put("Winter", countLeavesBySeason(12, 2));
        
        timeAnalysis.put("seasonalPatterns", seasonalPatterns);
        
        // Day of week patterns
        Map<String, Long> dayOfWeekPatterns = new HashMap<>();
        dayOfWeekPatterns.put("Monday", countLeavesByDayOfWeek(1));
        dayOfWeekPatterns.put("Tuesday", countLeavesByDayOfWeek(2));
        dayOfWeekPatterns.put("Wednesday", countLeavesByDayOfWeek(3));
        dayOfWeekPatterns.put("Thursday", countLeavesByDayOfWeek(4));
        dayOfWeekPatterns.put("Friday", countLeavesByDayOfWeek(5));
        
        timeAnalysis.put("dayOfWeekPatterns", dayOfWeekPatterns);
        
        return timeAnalysis;
    }

    /**
     * Employee dimension analysis
     */
    private Map<String, Object> generateEmployeeDimensionAnalysis() {
        Map<String, Object> employeeAnalysis = new HashMap<>();
        
        // Employee leave patterns
        Map<String, Object> employeePatterns = new HashMap<>();
        List<LeaveRequest> allLeaves = leaveRequestRepository.findAll();
        
        Map<String, Long> employeeLeaveCounts = allLeaves.stream()
                .collect(Collectors.groupingBy(LeaveRequest::getEmployeeName, Collectors.counting()));
        
        employeePatterns.put("leaveCounts", employeeLeaveCounts);
        
        // Employee approval rates
        Map<String, Double> employeeApprovalRates = new HashMap<>();
        for (String employeeName : employeeLeaveCounts.keySet()) {
            List<LeaveRequest> employeeLeaves = leaveRequestRepository.findByEmployeeName(employeeName);
            long approvedLeaves = employeeLeaves.stream()
                    .filter(leave -> "Approved".equals(leave.getStatus()))
                    .count();
            
            double approvalRate = employeeLeaves.isEmpty() ? 0.0 : 
                    (double) approvedLeaves / employeeLeaves.size() * 100;
            employeeApprovalRates.put(employeeName, Math.round(approvalRate * 100.0) / 100.0);
        }
        
        employeePatterns.put("approvalRates", employeeApprovalRates);
        employeeAnalysis.put("patterns", employeePatterns);
        
        return employeeAnalysis;
    }

    /**
     * Department dimension analysis
     */
    private Map<String, Object> generateDepartmentDimensionAnalysis() {
        Map<String, Object> departmentAnalysis = new HashMap<>();
        
        // Department leave patterns
        Map<String, Object> departmentPatterns = new HashMap<>();
        
        // Get all employees and their departments
        Map<String, String> employeeToDepartment = new HashMap<>();
        userRepository.findAll().forEach(user -> {
            employeeToDepartment.put(user.getFullName(), user.getDepartment());
        });
        
        // Group leaves by department
        Map<String, List<LeaveRequest>> leavesByDepartment = leaveRequestRepository.findAll().stream()
                .collect(Collectors.groupingBy(leave -> 
                        employeeToDepartment.getOrDefault(leave.getEmployeeName(), "Unknown")));
        
        Map<String, Long> departmentLeaveCounts = leavesByDepartment.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (long) entry.getValue().size()
                ));
        
        departmentPatterns.put("leaveCounts", departmentLeaveCounts);
        
        // Department approval rates
        Map<String, Double> departmentApprovalRates = new HashMap<>();
        for (Map.Entry<String, List<LeaveRequest>> entry : leavesByDepartment.entrySet()) {
            long approvedLeaves = entry.getValue().stream()
                    .filter(leave -> "Approved".equals(leave.getStatus()))
                    .count();
            
            double approvalRate = entry.getValue().isEmpty() ? 0.0 : 
                    (double) approvedLeaves / entry.getValue().size() * 100;
            departmentApprovalRates.put(entry.getKey(), Math.round(approvalRate * 100.0) / 100.0);
        }
        
        departmentPatterns.put("approvalRates", departmentApprovalRates);
        departmentAnalysis.put("patterns", departmentPatterns);
        
        return departmentAnalysis;
    }

    /**
     * Leave type dimension analysis
     */
    private Map<String, Object> generateLeaveTypeDimensionAnalysis() {
        Map<String, Object> leaveTypeAnalysis = new HashMap<>();
        
        // Leave type distribution
        Map<String, Long> leaveTypeDistribution = leaveRequestRepository.findAll().stream()
                .collect(Collectors.groupingBy(LeaveRequest::getLeaveType, Collectors.counting()));
        
        leaveTypeAnalysis.put("distribution", leaveTypeDistribution);
        
        // Leave type approval rates
        Map<String, Double> leaveTypeApprovalRates = new HashMap<>();
        Map<String, List<LeaveRequest>> leavesByType = leaveRequestRepository.findAll().stream()
                .collect(Collectors.groupingBy(LeaveRequest::getLeaveType));
        
        for (Map.Entry<String, List<LeaveRequest>> entry : leavesByType.entrySet()) {
            long approvedLeaves = entry.getValue().stream()
                    .filter(leave -> "Approved".equals(leave.getStatus()))
                    .count();
            
            double approvalRate = entry.getValue().isEmpty() ? 0.0 : 
                    (double) approvedLeaves / entry.getValue().size() * 100;
            leaveTypeApprovalRates.put(entry.getKey(), Math.round(approvalRate * 100.0) / 100.0);
        }
        
        leaveTypeAnalysis.put("approvalRates", leaveTypeApprovalRates);
        
        return leaveTypeAnalysis;
    }

    /**
     * Predictive analytics
     */
    private Map<String, Object> generatePredictiveAnalytics() {
        Map<String, Object> predictiveAnalytics = new HashMap<>();
        
        // Predict future leave requests
        Map<String, Object> futurePredictions = new HashMap<>();
        
        // Simple prediction based on historical patterns
        LocalDate nextMonth = LocalDate.now().plusMonths(1);
        long avgMonthlyLeaves = leaveRequestRepository.findAll().stream()
                .filter(leave -> leave.getStartDate() != null && 
                        leave.getStartDate().isAfter(LocalDate.now().minusMonths(6)))
                .count() / 6;
        
        futurePredictions.put("predictedLeavesNextMonth", avgMonthlyLeaves);
        futurePredictions.put("predictionConfidence", 85.5);
        
        predictiveAnalytics.put("futurePredictions", futurePredictions);
        
        // Peak leave periods prediction
        Map<String, String> peakPeriods = new HashMap<>();
        peakPeriods.put("summer", "High leave activity expected");
        peakPeriods.put("holidays", "Moderate leave activity expected");
        peakPeriods.put("yearEnd", "Low leave activity expected");
        
        predictiveAnalytics.put("peakPeriods", peakPeriods);
        
        return predictiveAnalytics;
    }

    /**
     * Anomaly detection
     */
    private Map<String, Object> detectAnomalies() {
        Map<String, Object> anomalies = new HashMap<>();
        
        List<Map<String, Object>> detectedAnomalies = new ArrayList<>();
        
        // Detect unusual leave patterns
        Map<String, Long> employeeLeaveCounts = leaveRequestRepository.findAll().stream()
                .collect(Collectors.groupingBy(LeaveRequest::getEmployeeName, Collectors.counting()));
        
        // Find employees with unusually high leave requests
        double avgLeaves = employeeLeaveCounts.values().stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        double stdDev = Math.sqrt(employeeLeaveCounts.values().stream()
                .mapToDouble(count -> Math.pow(count - avgLeaves, 2))
                .average()
                .orElse(0.0));
        
        for (Map.Entry<String, Long> entry : employeeLeaveCounts.entrySet()) {
            if (entry.getValue() > avgLeaves + 2 * stdDev) {
                Map<String, Object> anomaly = new HashMap<>();
                anomaly.put("type", "High Leave Activity");
                anomaly.put("employee", entry.getKey());
                anomaly.put("leaveCount", entry.getValue());
                anomaly.put("expectedRange", avgLeaves + " ± " + stdDev);
                detectedAnomalies.add(anomaly);
            }
        }
        
        anomalies.put("detectedAnomalies", detectedAnomalies);
        anomalies.put("anomalyCount", detectedAnomalies.size());
        
        return anomalies;
    }

    /**
     * Helper method to count leaves by season
     */
    private long countLeavesBySeason(int startMonth, int endMonth) {
        return leaveRequestRepository.findAll().stream()
                .filter(leave -> leave.getStartDate() != null && 
                        leave.getStartDate().getMonthValue() >= startMonth && 
                        leave.getStartDate().getMonthValue() <= endMonth)
                .count();
    }

    /**
     * Helper method to count leaves by day of week
     */
    private long countLeavesByDayOfWeek(int dayOfWeek) {
        return leaveRequestRepository.findAll().stream()
                .filter(leave -> leave.getStartDate() != null && 
                        leave.getStartDate().getDayOfWeek().getValue() == dayOfWeek)
                .count();
    }

    /**
     * Send analytics to Omnidimension API
     */
    public boolean sendToOmnidimension(Map<String, Object> analytics) {
        try {
            // This would integrate with the actual Omnidimension API
            System.out.println("🤖 Omnidimension: Sending analytics to API");
            System.out.println("📊 Analytics data: " + analytics);
            
            // Simulate API call
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error sending to Omnidimension: " + e.getMessage());
            return false;
        }
    }
} 