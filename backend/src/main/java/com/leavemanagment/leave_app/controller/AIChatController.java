package com.leavemanagment.leave_app.controller;

import com.leavemanagment.leave_app.service.AIChatAssistantService;
import com.leavemanagment.leave_app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AIChatController {

    @Autowired
    private AIChatAssistantService aiChatService;
    
    @Autowired
    private EmailService emailService;

    /**
     * WebSocket endpoint for AI chat
     */
    @MessageMapping("/ai-chat")
    @SendTo("/topic/ai-responses")
    public Map<String, Object> handleAIChat(Map<String, String> message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userMessage = message.get("message");
            String username = message.get("username");
            
            System.out.println("🤖 AI Chat: " + username + " asked: " + userMessage);
            
            // Process message through AI service
            String aiResponse = aiChatService.processChatMessage(userMessage, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "ai_response");
            response.put("message", aiResponse);
            response.put("username", username);
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("🤖 AI Response: " + aiResponse);
            
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ Error in AI chat: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "error");
            errorResponse.put("message", "Sorry, I encountered an error. Please try again.");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
    }

    /**
     * REST endpoint for AI chat (fallback)
     */
    @PostMapping("/api/ai-chat")
    @ResponseBody
    public Map<String, Object> handleAIChatRest(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            String username = request.get("username");
            
            System.out.println("🤖 AI Chat REST: " + username + " asked: " + userMessage);
            
            String aiResponse = aiChatService.processChatMessage(userMessage, username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", aiResponse);
            response.put("timestamp", System.currentTimeMillis());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ Error in AI chat REST: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Sorry, I encountered an error. Please try again.");
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
    }

    /**
     * Test AI chat functionality
     */
    @GetMapping("/api/ai-chat/test")
    @ResponseBody
    public Map<String, Object> testAIChat() {
        try {
            String testMessage = "help";
            String testUsername = "hr_user";
            
            String aiResponse = aiChatService.processChatMessage(testMessage, testUsername);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("test_message", testMessage);
            response.put("ai_response", aiResponse);
            response.put("timestamp", System.currentTimeMillis());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ Error testing AI chat: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
    }

    /**
     * Test email functionality
     */
    @GetMapping("/api/email/test")
    @ResponseBody
    public Map<String, Object> testEmail() {
        try {
            boolean emailConfigOk = emailService.testEmailConfiguration();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", emailConfigOk);
            response.put("message", emailConfigOk ? 
                "Email configuration is working" : 
                "Email configuration needs setup");
            response.put("timestamp", System.currentTimeMillis());
            
            return response;
            
        } catch (Exception e) {
            System.err.println("❌ Error testing email: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
    }
} 