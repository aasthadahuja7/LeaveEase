package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AINotificationService {

    @Value("${n8n.webhook.url:http://localhost:5678/webhook/leave-notification}")
    private String n8nWebhookUrl;

    @Value("${n8n.api.key:}")
    private String n8nApiKey;

    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Autowired
    private EmailService emailService;

    /**
     * Send AI-powered notification when leave is approved
     */
    public void sendLeaveApprovedNotification(LeaveRequest leaveRequest, User employee) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "LEAVE_APPROVED");
            notificationData.put("employeeName", employee.getFullName());
            notificationData.put("employeeEmail", employee.getEmail());
            notificationData.put("employeeUsername", employee.getUsername());
            notificationData.put("startDate", leaveRequest.getStartDate());
            notificationData.put("endDate", leaveRequest.getEndDate());
            notificationData.put("leaveType", leaveRequest.getLeaveType());
            notificationData.put("duration", leaveRequest.getLeaveDuration());
            notificationData.put("reason", leaveRequest.getReason());
            notificationData.put("leaveRequestId", leaveRequest.getId());
            notificationData.put("timestamp", LocalDateTime.now());
            notificationData.put("channels", new String[]{"email", "whatsapp", "slack"});
            notificationData.put("priority", "high");
            notificationData.put("action", "APPROVED");

            // Send to N8N webhook
            boolean n8nSuccess = sendToN8N(notificationData);
            
            // Fallback to direct email if N8N fails
            if (!n8nSuccess) {
                emailService.sendLeaveApprovedEmail(leaveRequest, employee);
            }
            
            System.out.println("🤖 AI Notification: Leave approved for " + employee.getFullName());
        } catch (Exception e) {
            System.err.println("❌ Error sending AI notification: " + e.getMessage());
        }
    }

    /**
     * Send AI-powered notification when leave is rejected
     */
    public void sendLeaveRejectedNotification(LeaveRequest leaveRequest, User employee, String rejectionReason) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "LEAVE_REJECTED");
            notificationData.put("employeeName", employee.getFullName());
            notificationData.put("employeeEmail", employee.getEmail());
            notificationData.put("employeeUsername", employee.getUsername());
            notificationData.put("startDate", leaveRequest.getStartDate());
            notificationData.put("endDate", leaveRequest.getEndDate());
            notificationData.put("leaveType", leaveRequest.getLeaveType());
            notificationData.put("duration", leaveRequest.getLeaveDuration());
            notificationData.put("reason", leaveRequest.getReason());
            notificationData.put("rejectionReason", rejectionReason != null ? rejectionReason : "No reason provided");
            notificationData.put("leaveRequestId", leaveRequest.getId());
            notificationData.put("timestamp", LocalDateTime.now());
            notificationData.put("channels", new String[]{"email", "whatsapp", "slack"});
            notificationData.put("priority", "high");
            notificationData.put("action", "REJECTED");

            // Send to N8N webhook
            boolean n8nSuccess = sendToN8N(notificationData);
            
            // Fallback to direct email if N8N fails
            if (!n8nSuccess) {
                emailService.sendLeaveRejectedEmail(leaveRequest, employee, rejectionReason);
            }
            
            System.out.println("🤖 AI Notification: Leave rejected for " + employee.getFullName());
        } catch (Exception e) {
            System.err.println("❌ Error sending AI notification: " + e.getMessage());
        }
    }

    /**
     * Send AI-powered reminder for upcoming leave
     */
    public void sendLeaveReminderNotification(LeaveRequest leaveRequest, User employee) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "LEAVE_REMINDER");
            notificationData.put("employeeName", employee.getFullName());
            notificationData.put("employeeEmail", employee.getEmail());
            notificationData.put("startDate", leaveRequest.getStartDate());
            notificationData.put("endDate", leaveRequest.getEndDate());
            notificationData.put("leaveType", leaveRequest.getLeaveType());
            notificationData.put("timestamp", LocalDateTime.now());
            notificationData.put("channels", new String[]{"email"});

            // Send to N8N webhook
            sendToN8N(notificationData);
            
            System.out.println("🤖 AI Notification: Leave reminder for " + employee.getFullName());
        } catch (Exception e) {
            System.err.println("❌ Error sending AI notification: " + e.getMessage());
        }
    }

    /**
     * Send notification to N8N webhook
     */
    private boolean sendToN8N(Map<String, Object> data) {
        try {
            WebClient webClient = webClientBuilder.build();
            
            String response = webClient.post()
                    .uri(n8nWebhookUrl)
                    .header("Content-Type", "application/json")
                    .header("X-N8N-API-Key", n8nApiKey)
                    .bodyValue(data)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            System.out.println("✅ N8N webhook response: " + response);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ N8N webhook error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test N8N connection
     */
    public boolean testN8NConnection() {
        try {
            Map<String, Object> testData = new HashMap<>();
            testData.put("type", "TEST");
            testData.put("message", "Testing N8N connection");
            testData.put("timestamp", LocalDateTime.now());

            WebClient webClient = webClientBuilder.build();
            
            String response = webClient.post()
                    .uri(n8nWebhookUrl)
                    .header("Content-Type", "application/json")
                    .header("X-N8N-API-Key", n8nApiKey)
                    .bodyValue(testData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ N8N connection test successful: " + response);
            return true;
        } catch (Exception e) {
            System.err.println("❌ N8N connection test failed: " + e.getMessage());
            return false;
        }
    }
} 