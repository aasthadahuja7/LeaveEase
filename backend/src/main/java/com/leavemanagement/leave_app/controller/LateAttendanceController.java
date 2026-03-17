package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.model.LateAttendance;
import com.leavemanagement.leave_app.service.LateAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/late-attendance")
public class LateAttendanceController {
    
    @Autowired
    private LateAttendanceService lateAttendanceService;
    
    /**
     * Mark an employee as late (HR only)
     */
    @PostMapping("/mark-late")
    public ResponseEntity<?> markEmployeeLate(@RequestBody Map<String, Object> request) {
        try {
            String employeeName = (String) request.get("employeeName");
            String dateStr = (String) request.get("date");
            String reason = (String) request.get("reason");
            String notes = (String) request.get("notes");
            
            // Get current HR user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String markedBy = auth.getName();
            
            LocalDate date = LocalDate.parse(dateStr);
            
            LateAttendance lateAttendance = lateAttendanceService.markEmployeeLate(
                employeeName, date, reason, markedBy, notes
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee marked as late successfully");
            response.put("data", lateAttendance);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error marking employee as late: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get late attendance records for current employee
     */
    @GetMapping("/my-late-records")
    public ResponseEntity<?> getMyLateRecords() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String employeeName = auth.getName();
            
            List<LateAttendance> lateRecords = lateAttendanceService.getLateAttendanceForEmployee(employeeName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", lateRecords);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching late records: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get late attendance records for a specific date (HR only)
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getLateRecordsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<LateAttendance> lateRecords = lateAttendanceService.getLateAttendanceForDate(date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", lateRecords);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching late records for date: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get late attendance records within a date range (HR only)
     */
    @GetMapping("/range")
    public ResponseEntity<?> getLateRecordsInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<LateAttendance> lateRecords = lateAttendanceService.getLateAttendanceInRange(startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", lateRecords);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching late records in range: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 