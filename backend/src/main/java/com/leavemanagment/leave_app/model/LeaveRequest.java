package com.leavemanagment.leave_app.model;
//  It is model to represent the leave form data
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Document(collection = "leave_requests")
public class LeaveRequest {

    @Id

    private String id;  // MongoDB uses String IDs by default

    private String employeeName;
    
    private String employeeId; // User ID for better linking to User entity

    private LocalDate startDate;

    private LocalDate endDate;

    private String reason;

    private String status; // Pending, Approved, Rejected
    
    private String leaveType; // Annual, Sick, Personal, etc.
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String rejectionReason; // New field for HR rejection reasons

    // Constructors
    public LeaveRequest() {
        this.status = "Pending";
        this.leaveType = "Annual";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LeaveRequest(String employeeName, LocalDate startDate, LocalDate endDate, String reason, String status) {
        this.employeeName = employeeName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.leaveType = "Annual";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public LeaveRequest(String employeeName, String employeeId, LocalDate startDate, LocalDate endDate, String reason, String status) {
        this.employeeName = employeeName;
        this.employeeId = employeeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = status;
        this.leaveType = "Annual";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    // Helper method to calculate leave duration in days
    public long getLeaveDuration() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1; // +1 to include both start and end dates
        }
        return 0;
    }
}
