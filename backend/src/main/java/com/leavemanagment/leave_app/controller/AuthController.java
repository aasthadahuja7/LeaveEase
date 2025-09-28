package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.model.Role;
import com.leavemanagment.leave_app.model.User;
import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.repository.UserRepository;
import com.leavemanagment.leave_app.repository.LeaveRequestRepository;
import com.leavemanagment.leave_app.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
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
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        // Check user role and redirect accordingly
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_HR")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/dashboard.html?role=hr";
        } else {
            return "redirect:/dashboard.html?role=employee";
        }
    }
    
    @GetMapping("/")
    public String home() {
        return "redirect:/login.html";
    }
    
    // Get current authenticated user information
    @GetMapping("/auth/current-user")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(response);
        }
        
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("fullName", user.getFullName());
                response.put("department", user.getDepartment());
                response.put("role", user.getRole().toString());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Manual endpoint to create users for testing
    @GetMapping("/create-users")
    @ResponseBody
    public String createUsers() {
        try {
            initializeDemoUsers();
            return "Users created successfully!";
        } catch (Exception e) {
            return "Error creating users: " + e.getMessage();
        }
    }
    
    // Test endpoint to verify user lookup
    @GetMapping("/test-user")
    @ResponseBody
    public String testUser() {
        try {
            Optional<User> user = userRepository.findByUsername("hr_user");
            if (user.isPresent()) {
                User u = user.get();
                boolean passwordMatch = passwordEncoder.matches("password123", u.getPassword());
                return "User found: " + u.getUsername() + " with role: " + u.getRole() + 
                       ", Password match: " + passwordMatch;
            } else {
                return "User not found";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // Debug endpoint to print received POST parameters
    @PostMapping("/debug-login")
    @ResponseBody
    public String debugLogin(@RequestParam Map<String, String> params) {
        return "Received: " + params;
    }
    
    // Initialize demo users and employees
    @PostConstruct
    public void initializeData() {
        System.out.println("🔄 AuthController PostConstruct called...");
        try {
            initializeDemoUsers();
            employeeService.initializeSampleEmployees();
            initializeSampleLeaveRequests();
            System.out.println("✅ Data initialization completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error during data initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeDemoUsers() {
        System.out.println("🔄 Checking if users exist...");
        long userCount = userRepository.count();
        System.out.println("📊 Current user count: " + userCount);
        
        if (userCount == 0) {
            System.out.println("🔄 Creating demo users...");
            
            // Create HR user
            User hrUser = new User();
            hrUser.setUsername("hr_user");
            hrUser.setEmail("hr@company.com");
            hrUser.setPassword(passwordEncoder.encode("password123"));
            hrUser.setFullName("HR Manager");
            hrUser.setDepartment("Human Resources");
            hrUser.setRole(Role.HR);
            
            // Create employee users
            User employee1 = new User();
            employee1.setUsername("john_doe");
            employee1.setEmail("john.doe@company.com");
            employee1.setPassword(passwordEncoder.encode("password123"));
            employee1.setFullName("John Doe");
            employee1.setDepartment("Engineering");
            employee1.setRole(Role.EMPLOYEE);
            
            User employee2 = new User();
            employee2.setUsername("jane_smith");
            employee2.setEmail("jane.smith@company.com");
            employee2.setPassword(passwordEncoder.encode("password123"));
            employee2.setFullName("Jane Smith");
            employee2.setDepartment("Engineering");
            employee2.setRole(Role.EMPLOYEE);
            
            User employee3 = new User();
            employee3.setUsername("mike_johnson");
            employee3.setEmail("mike.johnson@company.com");
            employee3.setPassword(passwordEncoder.encode("password123"));
            employee3.setFullName("Mike Johnson");
            employee3.setDepartment("Marketing");
            employee3.setRole(Role.EMPLOYEE);
            
            userRepository.save(hrUser);
            userRepository.save(employee1);
            userRepository.save(employee2);
            userRepository.save(employee3);
            
            System.out.println("✅ Demo users created successfully!");
            System.out.println("🔐 HR User: hr_user / password123");
            System.out.println("👤 Employee: john_doe / password123");
            System.out.println("👤 Employee: jane_smith / password123");
            System.out.println("👤 Employee: mike_johnson / password123");
        } else {
            System.out.println("ℹ️ Users already exist, skipping creation.");
        }
    }
    
    private void initializeSampleLeaveRequests() {
        System.out.println("🔄 Checking if leave requests exist...");
        long leaveRequestCount = leaveRequestRepository.count();
        System.out.println("📊 Current leave request count: " + leaveRequestCount);
        
        if (leaveRequestCount == 0) {
            System.out.println("🔄 Creating sample leave requests...");
            
            // Create sample leave requests
            LeaveRequest request1 = new LeaveRequest();
            request1.setEmployeeName("John Doe");
            request1.setStartDate(LocalDate.of(2025, 8, 15));
            request1.setEndDate(LocalDate.of(2025, 8, 20));
            request1.setLeaveType("Annual Leave");
            request1.setReason("Family vacation");
            request1.setStatus("Pending");
            request1.setCreatedAt(LocalDateTime.now());
            
            LeaveRequest request2 = new LeaveRequest();
            request2.setEmployeeName("Jane Smith");
            request2.setStartDate(LocalDate.of(2025, 8, 25));
            request2.setEndDate(LocalDate.of(2025, 8, 27));
            request2.setLeaveType("Sick Leave");
            request2.setReason("Medical appointment");
            request2.setStatus("Approved");
            request2.setCreatedAt(LocalDateTime.now().minusDays(5));
            
            LeaveRequest request3 = new LeaveRequest();
            request3.setEmployeeName("Mike Johnson");
            request3.setStartDate(LocalDate.of(2025, 9, 1));
            request3.setEndDate(LocalDate.of(2025, 9, 5));
            request3.setLeaveType("Annual Leave");
            request3.setReason("Business trip");
            request3.setStatus("Pending");
            request3.setCreatedAt(LocalDateTime.now().minusDays(2));
            
            LeaveRequest request4 = new LeaveRequest();
            request4.setEmployeeName("Sarah Wilson");
            request4.setStartDate(LocalDate.of(2025, 8, 10));
            request4.setEndDate(LocalDate.of(2025, 8, 12));
            request4.setLeaveType("Personal Leave");
            request4.setReason("Personal matters");
            request4.setStatus("Rejected");
            request4.setRejectionReason("Insufficient notice period");
            request4.setCreatedAt(LocalDateTime.now().minusDays(10));
            
            leaveRequestRepository.saveAll(java.util.List.of(request1, request2, request3, request4));
            
            System.out.println("✅ Sample leave requests created successfully!");
        } else {
            System.out.println("ℹ️ Leave requests already exist, skipping creation.");
        }
    }
    
    // Add this method to AuthController.java (after the existing methods, before the closing brace)
    @PostMapping("/signup")
    public String signup(@RequestParam String fullName,
                        @RequestParam String username, 
                        @RequestParam String email,
                        @RequestParam String password,
                        @RequestParam String department,
                        @RequestParam String role) {
        try {
            // Check if username already exists
            if (userRepository.findByUsername(username).isPresent()) {
                return "redirect:/signup.html?error=true&type=username";
            }
            
            // Check if email already exists
            if (userRepository.findByEmail(email).isPresent()) {
                return "redirect:/signup.html?error=true&type=email";
            }
            
            // Create new user
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password)); // Encrypt password
            newUser.setFullName(fullName);
            newUser.setDepartment(department);
            
            // Set role (convert string to Role enum)
            if ("HR".equals(role)) {
                newUser.setRole(Role.HR);
            } else {
                newUser.setRole(Role.EMPLOYEE);
            }
            
            // Save user to database
            userRepository.save(newUser);
            
            // Create corresponding employee record
            employeeService.createEmployeeFromUser(newUser);
            
            // Redirect to login with success message
            return "redirect:/login.html?signup=success";
            
        } catch (Exception e) {
            System.err.println("Error during signup: " + e.getMessage());
            return "redirect:/signup.html?error=true";
        }
    }

}