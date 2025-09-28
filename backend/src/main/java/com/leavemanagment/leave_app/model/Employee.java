package com.leavemanagment.leave_app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;

@Document(collection = "employees")
public class Employee {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String employeeId; // Unique employee ID like EMP001
    
    private String fullName;
    private String email;
    private String department;
    private String position;
    private LocalDate joinDate;
    private String phoneNumber;
    private boolean isActive; // true = present, false = inactive
    private boolean isOnLeave; // true = currently on leave
    private String managerId; // Reference to manager's employee ID
    
    // Leave balance information
    private int totalLeaveEntitlement; // Total leaves per year
    private int usedLeaves; // Leaves taken this year
    private int remainingLeaves; // Remaining leaves
    
    // Constructors
    public Employee() {}
    
    public Employee(String employeeId, String fullName, String email, String department, 
                   String position, LocalDate joinDate, String phoneNumber) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.position = position;
        this.joinDate = joinDate;
        this.phoneNumber = phoneNumber;
        this.isActive = true;
        this.isOnLeave = false;
        this.totalLeaveEntitlement = 25; // Default 25 days per year
        this.usedLeaves = 0;
        this.remainingLeaves = 25;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public LocalDate getJoinDate() { return joinDate; }
    public void setJoinDate(LocalDate joinDate) { this.joinDate = joinDate; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public boolean isOnLeave() { return isOnLeave; }
    public void setOnLeave(boolean onLeave) { isOnLeave = onLeave; }
    
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    
    public int getTotalLeaveEntitlement() { return totalLeaveEntitlement; }
    public void setTotalLeaveEntitlement(int totalLeaveEntitlement) { this.totalLeaveEntitlement = totalLeaveEntitlement; }
    
    public int getUsedLeaves() { return usedLeaves; }
    public void setUsedLeaves(int usedLeaves) { this.usedLeaves = usedLeaves; }
    
    public int getRemainingLeaves() { return remainingLeaves; }
    public void setRemainingLeaves(int remainingLeaves) { this.remainingLeaves = remainingLeaves; }
    
    // Helper methods
    public void updateLeaveBalance() {
        this.remainingLeaves = this.totalLeaveEntitlement - this.usedLeaves;
    }
    
    public boolean canTakeLeave(int daysRequested) {
        return this.remainingLeaves >= daysRequested;
    }
}