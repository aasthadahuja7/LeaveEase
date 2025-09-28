package com.leavemanagment.leave_app.repository;

import com.leavemanagment.leave_app.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    
    // Find employee by employee ID
    Optional<Employee> findByEmployeeId(String employeeId);
    
    // Find employee by email
    Optional<Employee> findByEmail(String email);
    
    // Find all active employees
    List<Employee> findByIsActiveTrue();
    
    // Find all employees currently on leave
    List<Employee> findByIsOnLeaveTrue();
    
    // Find employees by department
    List<Employee> findByDepartment(String department);
    
    // Count total employees
    long countByIsActiveTrue();
    
    // Count employees on leave
    long countByIsOnLeaveTrue();
    
    // Count employees present (active and not on leave)
    @Query("{ 'isActive': true, 'isOnLeave': false }")
    long countEmployeesPresent();
    
    // Find employees by manager
    List<Employee> findByManagerId(String managerId);
    
    // Check if employee ID exists
    boolean existsByEmployeeId(String employeeId);
    
    // Find employee by full name
    Optional<Employee> findByFullName(String fullName);
}