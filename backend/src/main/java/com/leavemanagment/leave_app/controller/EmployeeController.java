package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.Employee;
import com.leavemanagment.leave_app.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * Get all active employees
     */
    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<Employee> employees = employeeService.getAllActiveEmployees();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", employees);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching employees: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get employees by department
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<?> getEmployeesByDepartment(@PathVariable String department) {
        try {
            List<Employee> employees = employeeService.getEmployeesByDepartment(department);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", employees);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching employees by department: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get employee by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable String id) {
        try {
            var employee = employeeService.getEmployeeById(id);
            
            if (employee.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", employee.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Employee not found");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching employee: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 