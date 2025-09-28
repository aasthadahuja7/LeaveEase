package com.leavemanagment.leave_app.service;
import com.leavemanagment.leave_app.model.User;

import com.leavemanagment.leave_app.model.Employee;
import com.leavemanagment.leave_app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    // Get all active employees
    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }
    
    // Get employee by ID
    public Optional<Employee> getEmployeeById(String id) {
        return employeeRepository.findById(id);
    }
    
    // Get employee by employee ID
    public Optional<Employee> getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }
    
    // Get employee by email
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }
    
    // Save employee
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    
    // Get HR dashboard statistics
    public EmployeeStats getEmployeeStats() {
        long totalEmployees = employeeRepository.countByIsActiveTrue();
        long employeesOnLeave = employeeRepository.countByIsOnLeaveTrue();
        long employeesPresent = employeeRepository.countEmployeesPresent();
        
        return new EmployeeStats(totalEmployees, employeesOnLeave, employeesPresent);
    }
    
    // Get employees currently on leave
    public List<Employee> getEmployeesOnLeave() {
        return employeeRepository.findByIsOnLeaveTrue();
    }
    
    // Get employees by department
    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }
    
    // Get employee ID by name
    public String getEmployeeIdByName(String employeeName) {
        Optional<Employee> employee = employeeRepository.findByFullName(employeeName);
        return employee.map(Employee::getEmployeeId).orElse(null);
    }
    
    // Update employee leave status
    public void updateEmployeeLeaveStatus(String employeeId, boolean isOnLeave) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setOnLeave(isOnLeave);
            employeeRepository.save(employee);
        }
    }
    
    // Update employee leave balance
    public void updateLeaveBalance(String employeeId, int daysUsed) {
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeId(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setUsedLeaves(employee.getUsedLeaves() + daysUsed);
            employee.updateLeaveBalance();
            employeeRepository.save(employee);
        }
    }
    
    // Initialize sample employees (for development)
    public void initializeSampleEmployees() {
        if (employeeRepository.count() == 0) {
            // Create sample employees
            Employee emp1 = new Employee("EMP001", "John Doe", "john.doe@company.com", 
                                       "Engineering", "Software Developer", LocalDate.of(2022, 1, 15), "123-456-7890");
            emp1.setUsedLeaves(5);
            emp1.updateLeaveBalance();
            
            Employee emp2 = new Employee("EMP002", "Jane Smith", "jane.smith@company.com", 
                                       "Engineering", "Senior Developer", LocalDate.of(2021, 3, 10), "123-456-7891");
            emp2.setUsedLeaves(8);
            emp2.setOnLeave(true);
            emp2.updateLeaveBalance();
            
            Employee emp3 = new Employee("EMP003", "Mike Johnson", "mike.johnson@company.com", 
                                       "Marketing", "Marketing Manager", LocalDate.of(2020, 6, 5), "123-456-7892");
            emp3.setUsedLeaves(12);
            emp3.updateLeaveBalance();
            
            Employee emp4 = new Employee("EMP004", "Sarah Wilson", "sarah.wilson@company.com", 
                                       "HR", "HR Manager", LocalDate.of(2019, 9, 20), "123-456-7893");
            emp4.setUsedLeaves(3);
            emp4.updateLeaveBalance();
            
            Employee emp5 = new Employee("EMP005", "David Brown", "david.brown@company.com", 
                                       "Engineering", "DevOps Engineer", LocalDate.of(2023, 2, 1), "123-456-7894");
            emp5.setUsedLeaves(2);
            emp5.setOnLeave(true);
            emp5.updateLeaveBalance();
            
            employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4, emp5));
        }
    }
    
    // Add this method after initializeSampleEmployees() and before the EmployeeStats class
    public void createEmployeeFromUser(User user) {
        try {
            // Check if employee already exists for this user by email
            Optional<Employee> existingEmployee = employeeRepository.findByEmail(user.getEmail());
            if (existingEmployee.isPresent()) {
                return; // Employee already exists, don't create duplicate
            }
            
            // Generate unique employee ID
            String employeeId = generateEmployeeId(user);
            
            // Create new employee record
            Employee employee = new Employee(
                employeeId,
                user.getFullName(),
                user.getEmail(),
                user.getDepartment(),
                "Employee", // Default position
                LocalDate.now(), // Join date
                "" // Phone number (empty for now)
            );
            
            // Set default values for new employees
            employee.setTotalLeaveEntitlement(25); // 25 days annual leave
            employee.setUsedLeaves(0);
            employee.setRemainingLeaves(25);
            employee.setActive(true); // Active by default
            
            // Save employee to database
            employeeRepository.save(employee);
            
            System.out.println("Created employee record for user: " + user.getUsername() + " with ID: " + employeeId);
            
        } catch (Exception e) {
            System.err.println("Error creating employee record: " + e.getMessage());
            throw new RuntimeException("Failed to create employee record", e);
        }
    }
    
    private String generateEmployeeId(User user) {
        // Generate employee ID based on department and timestamp
        String deptCode = user.getDepartment().substring(0, Math.min(3, user.getDepartment().length())).toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return deptCode + timestamp;
    }

    
    // Inner class for employee statistics
    public static class EmployeeStats {
        private long totalEmployees;
        private long employeesOnLeave;
        private long employeesPresent;
        
        public EmployeeStats(long totalEmployees, long employeesOnLeave, long employeesPresent) {
            this.totalEmployees = totalEmployees;
            this.employeesOnLeave = employeesOnLeave;
            this.employeesPresent = employeesPresent;
        }
        
        // Getters
        public long getTotalEmployees() { return totalEmployees; }
        public long getEmployeesOnLeave() { return employeesOnLeave; }
        public long getEmployeesPresent() { return employeesPresent; }
    }
}