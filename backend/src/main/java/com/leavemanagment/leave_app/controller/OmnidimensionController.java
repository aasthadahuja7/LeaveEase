package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.service.OmnidimensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/omnidimension")
@CrossOrigin(origins = "*")
public class OmnidimensionController {

    @Autowired
    private OmnidimensionService omnidimensionService;

    /**
     * Get multi-dimensional analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        try {
            System.out.println("🤖 Omnidimension: Generating analytics...");
            Map<String, Object> analytics = omnidimensionService.generateMultiDimensionalAnalytics();
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            System.err.println("❌ Error generating analytics: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get time dimension analysis
     */
    @GetMapping("/analytics/time")
    public ResponseEntity<Map<String, Object>> getTimeAnalysis() {
        try {
            Map<String, Object> analytics = omnidimensionService.generateMultiDimensionalAnalytics();
            Map<String, Object> timeAnalysis = (Map<String, Object>) analytics.get("timeAnalysis");
            
            return ResponseEntity.ok(timeAnalysis);
        } catch (Exception e) {
            System.err.println("❌ Error getting time analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get employee dimension analysis
     */
    @GetMapping("/analytics/employees")
    public ResponseEntity<Map<String, Object>> getEmployeeAnalysis() {
        try {
            Map<String, Object> analytics = omnidimensionService.generateMultiDimensionalAnalytics();
            Map<String, Object> employeeAnalysis = (Map<String, Object>) analytics.get("employeeAnalysis");
            
            return ResponseEntity.ok(employeeAnalysis);
        } catch (Exception e) {
            System.err.println("❌ Error getting employee analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get department dimension analysis
     */
    @GetMapping("/analytics/departments")
    public ResponseEntity<Map<String, Object>> getDepartmentAnalysis() {
        try {
            Map<String, Object> analytics = omnidimensionService.generateMultiDimensionalAnalytics();
            Map<String, Object> departmentAnalysis = (Map<String, Object>) analytics.get("departmentAnalysis");
            
            return ResponseEntity.ok(departmentAnalysis);
        } catch (Exception e) {
            System.err.println("❌ Error getting department analysis: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get predictive analytics
     */
    @GetMapping("/analytics/predictive")
    public ResponseEntity<Map<String, Object>> getPredictiveAnalytics() {
        try {
            Map<String, Object> analytics = omnidimensionService.generateMultiDimensionalAnalytics();
            Map<String, Object> predictiveAnalytics = (Map<String, Object>) analytics.get("predictiveAnalytics");
            
            return ResponseEntity.ok(predictiveAnalytics);
        } catch (Exception e) {
            System.err.println("❌ Error getting predictive analytics: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get anomaly detection results
     */
    @GetMapping("/analytics/anomalies")
    public ResponseEntity<Map<String, Object>> getAnomalies() {
        try {
            Map<String, Object> analytics = omnidimensionService.generateMultiDimensionalAnalytics();
            Map<String, Object> anomalies = (Map<String, Object>) analytics.get("anomalyDetection");
            
            return ResponseEntity.ok(anomalies);
        } catch (Exception e) {
            System.err.println("❌ Error getting anomalies: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test Omnidimension connection
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        try {
            Map<String, Object> analytics = omnidimensionService.generateMultiDimensionalAnalytics();
            boolean sent = omnidimensionService.sendToOmnidimension(analytics);
            
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Omnidimension integration is working",
                "analyticsGenerated", true,
                "sentToOmnidimension", sent
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error testing Omnidimension connection: " + e.getMessage());
            
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Omnidimension integration failed: " + e.getMessage(),
                "analyticsGenerated", false,
                "sentToOmnidimension", false
            );
            
            return ResponseEntity.ok(response);
        }
    }
} 