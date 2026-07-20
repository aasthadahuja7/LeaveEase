package com.leavemanagement.leave_app.service;

import com.leavemanagement.leave_app.model.Employee;
import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }

    public Optional<Employee> getEmployeeById(String id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }

    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public Employee saveEmployee(Employee employee) {
        validateEmail(employee.getEmail());
        return employeeRepository.save(employee);
    }

    private void validateEmail(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            throw new IllegalArgumentException(
                    "Email must be a valid Gmail address (example@gmail.com)"
            );
        }

        if (employeeRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email address is already registered");
        }
    }

    public EmployeeStats getEmployeeStats() {

        return new EmployeeStats(
                employeeRepository.countByIsActiveTrue(),
                employeeRepository.countByIsOnLeaveTrue(),
                employeeRepository.countByIsActiveTrueAndIsOnLeaveFalse()
        );
    }

    public List<Employee> getEmployeesOnLeave() {
        return employeeRepository.findByIsOnLeaveTrue();
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        return employeeRepository.findByDepartment(department);
    }

    public String getEmployeeIdByName(String employeeName) {
        return employeeRepository.findByFullName(employeeName)
                .map(Employee::getEmployeeId)
                .orElse(null);
    }

    public void updateEmployeeLeaveStatus(String employeeId, boolean isOnLeave) {

        employeeRepository.findByEmployeeId(employeeId).ifPresent(employee -> {
            employee.setOnLeave(isOnLeave);
            employeeRepository.save(employee);
        });
    }

    public void updateLeaveBalance(String employeeId, int daysUsed) {

        employeeRepository.findByEmployeeId(employeeId).ifPresent(employee -> {
            employee.setUsedLeaves(employee.getUsedLeaves() + daysUsed);
            employee.updateLeaveBalance();
            employeeRepository.save(employee);
        });
    }

    public void initializeSampleEmployees() {

        if (employeeRepository.count() != 0) {
            return;
        }

        Employee emp1 = new Employee(
                "EMP001", "John Doe", "john.doe@gmail.com",
                "Engineering", "Software Developer",
                LocalDate.of(2022, 1, 15), "123-456-7890"
        );
        emp1.setUsedLeaves(5);
        emp1.updateLeaveBalance();

        Employee emp2 = new Employee(
                "EMP002", "Jane Smith", "jane.smith@gmail.com",
                "Engineering", "Senior Developer",
                LocalDate.of(2021, 3, 10), "123-456-7891"
        );
        emp2.setUsedLeaves(8);
        emp2.setOnLeave(true);
        emp2.updateLeaveBalance();

        Employee emp3 = new Employee(
                "EMP003", "Mike Johnson", "mike.johnson@gmail.com",
                "Marketing", "Marketing Manager",
                LocalDate.of(2020, 6, 5), "123-456-7892"
        );
        emp3.setUsedLeaves(12);
        emp3.updateLeaveBalance();

        Employee emp4 = new Employee(
                "EMP004", "Sarah Wilson", "sarah.wilson@gmail.com",
                "HR", "HR Manager",
                LocalDate.of(2019, 9, 20), "123-456-7893"
        );
        emp4.setUsedLeaves(3);
        emp4.updateLeaveBalance();

        Employee emp5 = new Employee(
                "EMP005", "David Brown", "david.brown@gmail.com",
                "Engineering", "DevOps Engineer",
                LocalDate.of(2023, 2, 1), "123-456-7894"
        );
        emp5.setUsedLeaves(2);
        emp5.setOnLeave(true);
        emp5.updateLeaveBalance();

        employeeRepository.saveAll(List.of(emp1, emp2, emp3, emp4, emp5));
    }

    public void createEmployeeFromUser(User user) {

        String employeeId = generateEmployeeId(user);

        Employee employee = new Employee(
                employeeId,
                user.getFullName(),
                user.getEmail(),
                user.getDepartment(),
                "Employee",
                LocalDate.now(),
                ""
        );

        employee.setTotalLeaveEntitlement(25);
        employee.setUsedLeaves(0);
        employee.setRemainingLeaves(25);
        employee.setActive(true);

        employeeRepository.save(employee);
    }

    private String generateEmployeeId(User user) {

        String department = user.getDepartment() == null ? "EMP" : user.getDepartment().trim();

        if (department.isEmpty()) {
            department = "EMP";
        }

        String deptCode = department.substring(0, Math.min(3, department.length())).toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);

        return deptCode + timestamp;
    }

    public static class EmployeeStats {

        private long totalEmployees;
        private long employeesOnLeave;
        private long employeesPresent;

        public EmployeeStats(long totalEmployees,
                             long employeesOnLeave,
                             long employeesPresent) {
            this.totalEmployees = totalEmployees;
            this.employeesOnLeave = employeesOnLeave;
            this.employeesPresent = employeesPresent;
        }

        public long getTotalEmployees() {
            return totalEmployees;
        }

        public long getEmployeesOnLeave() {
            return employeesOnLeave;
        }

        public long getEmployeesPresent() {
            return employeesPresent;
        }
    }
}