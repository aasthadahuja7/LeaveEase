package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.service.AINotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private AINotificationService aiNotificationService;

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
                response.put("message", "N8N connection failed - will use fallback");
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
            // Only test N8N since email services were removed
            boolean n8nTest = aiNotificationService.testN8NConnection();
            
            if (n8nTest) {
                response.put("status", "success");
                response.put("message", "Notification system (N8N) is working");
                System.out.println("✅ Notification test successful");
            } else {
                response.put("status", "error");
                response.put("message", "Notification system (N8N) not configured properly");
                System.out.println("❌ Notification test failed");
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Test error: " + e.getMessage());
            System.err.println("❌ Notification test error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
