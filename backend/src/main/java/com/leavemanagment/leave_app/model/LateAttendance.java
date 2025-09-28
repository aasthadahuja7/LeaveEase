package com.leavemanagment.leave_app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "late_attendance")
public class LateAttendance {
    
    @Id
    private String id;
    
    private String employeeName;
    private String employeeId;
    private LocalDate date;
    private String reason;
    private String markedBy; // HR who marked this
    private LocalDateTime markedAt;
    private String notes; // Additional notes from HR
    
    // Constructors
    public LateAttendance() {}
    
    public LateAttendance(String employeeName, String employeeId, LocalDate date, String reason, String markedBy) {
        this.employeeName = employeeName;
        this.employeeId = employeeId;
        this.date = date;
        this.reason = reason;
        this.markedBy = markedBy;
        this.markedAt = LocalDateTime.now();
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
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getMarkedBy() {
        return markedBy;
    }
    
    public void setMarkedBy(String markedBy) {
        this.markedBy = markedBy;
    }
    
    public LocalDateTime getMarkedAt() {
        return markedAt;
    }
    
    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
} 