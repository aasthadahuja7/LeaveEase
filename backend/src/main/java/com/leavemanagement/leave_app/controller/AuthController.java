package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.model.LeaveRequest;
import com.leavemanagement.leave_app.model.Role;
import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.repository.LeaveRequestRepository;
import com.leavemanagement.leave_app.repository.UserRepository;
import com.leavemanagement.leave_app.service.EmployeeService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // Redirect APIs
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HR"))
                || authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/dashboard.html?role=hr";
        }
        return "redirect:/dashboard.html?role=employee";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForward() {
        return "forward:/login.html";
    }

    // Authentication APIs
    @GetMapping("/auth/current-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(new HashMap<>());
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("fullName", user.getFullName());
                    response.put("department", user.getDepartment());
                    response.put("role", user.getRole().toString());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(new HashMap<>()));
    }

    // Signup
    @PostMapping("/signup")
    public String signup(@RequestParam String fullName,
                         @RequestParam String username,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String department,
                         @RequestParam String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/signup.html?error=true&type=username";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return "redirect:/signup.html?error=true&type=email";
        }

        User newUser = createUser(fullName, username, email, password, department, "HR".equals(role) ? Role.HR : Role.EMPLOYEE);
        userRepository.save(newUser);
        employeeService.createEmployeeFromUser(newUser);

        return "redirect:/login.html?signup=success";
    }

    // Initialization
    @PostConstruct
    public void initializeData() {
        initializeDemoUsers();
        employeeService.initializeSampleEmployees();
        initializeSampleLeaveRequests();
    }

    private void initializeDemoUsers() {
        if (userRepository.count() != 0) {
            return;
        }

        userRepository.save(createUser("HR Manager", "hr_user", "hr@company.com", "password123", "Human Resources", Role.HR));
        userRepository.save(createUser("John Doe", "john_doe", "john.doe@company.com", "password123", "Engineering", Role.EMPLOYEE));
        userRepository.save(createUser("Jane Smith", "jane_smith", "jane.smith@company.com", "password123", "Engineering", Role.EMPLOYEE));
        userRepository.save(createUser("Mike Johnson", "mike_johnson", "mike.johnson@company.com", "password123", "Marketing", Role.EMPLOYEE));
    }

    private void initializeSampleLeaveRequests() {
        if (leaveRequestRepository.count() != 0) {
            return;
        }

        leaveRequestRepository.saveAll(List.of(
                createLeaveRequest("John Doe", LocalDate.of(2025, 8, 15), LocalDate.of(2025, 8, 20), "Annual Leave", "Family vacation", "Pending", LocalDateTime.now(), null),
                createLeaveRequest("Jane Smith", LocalDate.of(2025, 8, 25), LocalDate.of(2025, 8, 27), "Sick Leave", "Medical appointment", "Approved", LocalDateTime.now().minusDays(5), null),
                createLeaveRequest("Mike Johnson", LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5), "Annual Leave", "Business trip", "Pending", LocalDateTime.now().minusDays(2), null),
                createLeaveRequest("Sarah Wilson", LocalDate.of(2025, 8, 10), LocalDate.of(2025, 8, 12), "Personal Leave", "Personal matters", "Rejected", LocalDateTime.now().minusDays(10), "Insufficient notice period"))
        );
    }

    // Helper Methods
    private User createUser(String fullName, String username, String email, String password, String department, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setDepartment(department);
        user.setRole(role);
        return user;
    }

    private LeaveRequest createLeaveRequest(String employeeName,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            String leaveType,
                                            String reason,
                                            String status,
                                            LocalDateTime createdAt,
                                            String rejectionReason) {
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeName(employeeName);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setLeaveType(leaveType);
        request.setReason(reason);
        request.setStatus(status);
        request.setCreatedAt(createdAt);
        request.setRejectionReason(rejectionReason);
        return request;
    }
}