package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.service.AINotificationService;
import com.leavemanagment.leave_app.service.EmailService;
import com.leavemanagment.leave_app.service.EmployeeEmailService;
import com.leavemanagment.leave_app.service.HREmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AINotificationService aiNotificationService;

    @Autowired
    private EmployeeEmailService employeeEmailService;

    @Autowired
    private HREmailService hrEmailService;

    /**
     * Test email configuration
     */
    @PostMapping("/test-email")
    public ResponseEntity<Map<String, String>> testEmail() {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean emailTest = emailService.testEmailConfiguration();
            if (emailTest) {
                response.put("status", "success");
                response.put("message", "Email configuration is working correctly");
                System.out.println("✅ Email test successful");
            } else {
                response.put("status", "error");
                response.put("message", "Email configuration failed");
                System.out.println("❌ Email test failed");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Email test error: " + e.getMessage());
            System.err.println("❌ Email test error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test N8N connection
     */
    @PostMapping("/test-n8n")
    public ResponseEntity<Map<String, String>> testN8N() {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean n8nTest = aiNotificationService.testN8NConnection();
            if (n8nTest) {
                response.put("status", "success");
                response.put("message", "N8N connection is working correctly");
                System.out.println("✅ N8N test successful");
            } else {
                response.put("status", "warning");
                response.put("message", "N8N connection failed - will use fallback email");
                System.out.println("⚠️ N8N test failed - using fallback");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "N8N test error: " + e.getMessage());
            System.err.println("❌ N8N test error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test complete notification system
     */
    @PostMapping("/test-notification")
    public ResponseEntity<Map<String, String>> testNotification() {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Test both email and N8N
            boolean emailTest = emailService.testEmailConfiguration();
            boolean n8nTest = aiNotificationService.testN8NConnection();
            
            if (emailTest && n8nTest) {
                response.put("status", "success");
                response.put("message", "Complete notification system is working");
                System.out.println("✅ Complete notification test successful");
            } else if (emailTest) {
                response.put("status", "partial");
                response.put("message", "Email working, N8N failed - using fallback");
                System.out.println("⚠️ Partial success - email working, N8N failed");
            } else {
                response.put("status", "error");
                response.put("message", "Notification system not configured properly");
                System.out.println("❌ Notification system test failed");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Test error: " + e.getMessage());
            System.err.println("❌ Notification test error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test employee email system
     */
    @PostMapping("/test-employee-email")
    public ResponseEntity<Map<String, String>> testEmployeeEmail() {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean employeeEmailTest = employeeEmailService.testEmployeeEmailConfiguration();
            if (employeeEmailTest) {
                response.put("status", "success");
                response.put("message", "Employee email system is working correctly");
                System.out.println("✅ Employee email test successful");
            } else {
                response.put("status", "error");
                response.put("message", "Employee email system failed");
                System.out.println("❌ Employee email test failed");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Employee email test error: " + e.getMessage());
            System.err.println("❌ Employee email test error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test HR email system
     */
    @PostMapping("/test-hr-email")
    public ResponseEntity<Map<String, String>> testHREmail(org.springframework.security.core.Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean hrEmailTest = hrEmailService.testHREmailConfiguration(authentication);
            if (hrEmailTest) {
                response.put("status", "success");
                response.put("message", "HR email system is working correctly");
                System.out.println("✅ HR email test successful");
            } else {
                response.put("status", "error");
                response.put("message", "HR email system failed");
                System.out.println("❌ HR email test failed");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "HR email test error: " + e.getMessage());
            System.err.println("❌ HR email test error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
