package com.leavemanagement.leave_app.controller;

import com.leavemanagement.leave_app.service.AIChatAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AIChatController {

    @Autowired
    private AIChatAssistantService aiChatService;

    @MessageMapping("/ai-chat")
    @SendTo("/topic/ai-responses")
    public Map<String, Object> handleAIChat(Map<String, String> message, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userMessage = message.get("message");
            String username = message.get("username");
            String aiResponse = aiChatService.processChatMessage(userMessage, username);
            return buildSocketResponse(aiResponse, username);
        } catch (Exception e) {
            return buildSocketErrorResponse();
        }
    }

    @PostMapping("/api/ai-chat")
    @ResponseBody
    public Map<String, Object> handleAIChatRest(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            String username = request.get("username");
            String aiResponse = aiChatService.processChatMessage(userMessage, username);
            return buildRestSuccessResponse(aiResponse);
        } catch (Exception e) {
            return buildRestErrorResponse();
        }
    }

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
            return buildTestErrorResponse(e.getMessage());
        }
    }

    private Map<String, Object> buildSocketResponse(String aiResponse, String username) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "ai_response");
        response.put("message", aiResponse);
        response.put("username", username);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> buildSocketErrorResponse() {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("type", "error");
        errorResponse.put("message", "Sorry, I encountered an error. Please try again.");
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    private Map<String, Object> buildRestSuccessResponse(String aiResponse) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", aiResponse);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> buildRestErrorResponse() {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Sorry, I encountered an error. Please try again.");
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    private Map<String, Object> buildTestErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorMessage);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}
