package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LateAttendance;
import com.leavemanagment.leave_app.repository.LateAttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LateAttendanceService {
    
    @Autowired
    private LateAttendanceRepository lateAttendanceRepository;
    
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * Mark an employee as late on a specific date
     */
    public LateAttendance markEmployeeLate(String employeeName, LocalDate date, String reason, String markedBy, String notes) {
        // Check if already marked as late on this date
        if (lateAttendanceRepository.existsByEmployeeNameAndDate(employeeName, date)) {
            throw new RuntimeException("Employee already marked as late on " + date);
        }
        
        // Get employee ID from employee service
        String employeeId = employeeService.getEmployeeIdByName(employeeName);
        
        LateAttendance lateAttendance = new LateAttendance(employeeName, employeeId, date, reason, markedBy);
        lateAttendance.setNotes(notes);
        
        return lateAttendanceRepository.save(lateAttendance);
    }
    
    /**
     * Get all late attendance records for an employee
     */
    public List<LateAttendance> getLateAttendanceForEmployee(String employeeName) {
        return lateAttendanceRepository.findByEmployeeNameOrderByDateDesc(employeeName);
    }
    
    /**
     * Get late attendance records for an employee within a date range
     */
    public List<LateAttendance> getLateAttendanceForEmployeeInRange(String employeeName, LocalDate startDate, LocalDate endDate) {
        return lateAttendanceRepository.findByEmployeeNameAndDateBetween(employeeName, startDate, endDate);
    }
    
    /**
     * Get all late attendance records for a specific date
     */
    public List<LateAttendance> getLateAttendanceForDate(LocalDate date) {
        return lateAttendanceRepository.findByDate(date);
    }
    
    /**
     * Get all late attendance records within a date range
     */
    public List<LateAttendance> getLateAttendanceInRange(LocalDate startDate, LocalDate endDate) {
        return lateAttendanceRepository.findByDateBetween(startDate, endDate);
    }
    
    /**
     * Check if an employee was late on a specific date
     */
    public boolean isEmployeeLateOnDate(String employeeName, LocalDate date) {
        return lateAttendanceRepository.existsByEmployeeNameAndDate(employeeName, date);
    }
    
    /**
     * Get a specific late attendance record by ID
     */
    public Optional<LateAttendance> getLateAttendanceById(String id) {
        return lateAttendanceRepository.findById(id);
    }
    
    /**
     * Update late attendance record
     */
    public LateAttendance updateLateAttendance(String id, String reason, String notes) {
        Optional<LateAttendance> optional = lateAttendanceRepository.findById(id);
        if (optional.isPresent()) {
            LateAttendance lateAttendance = optional.get();
            lateAttendance.setReason(reason);
            lateAttendance.setNotes(notes);
            return lateAttendanceRepository.save(lateAttendance);
        }
        throw new RuntimeException("Late attendance record not found with ID: " + id);
    }
    
    /**
     * Delete late attendance record
     */
    public void deleteLateAttendance(String id) {
        lateAttendanceRepository.deleteById(id);
    }
    
    /**
     * Get count of late days for an employee in a month
     */
    public long getLateDaysCountForEmployeeInMonth(String employeeName, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        List<LateAttendance> lateRecords = lateAttendanceRepository.findByEmployeeNameAndDateBetween(employeeName, startDate, endDate);
        return lateRecords.size();
    }
} 