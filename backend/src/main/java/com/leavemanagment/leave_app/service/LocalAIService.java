package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class LocalAIService {

    @Value("${ollama.api.url:http://localhost:11434/api/generate}")
    private String ollamaApiUrl;

    @Value("${ollama.model:llama2}")
    private String ollamaModel;

    private final WebClient webClient;

    public LocalAIService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Generate AI email using local Ollama
     */
    public String generateEmailWithOllama(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ollamaModel);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            String response = webClient.post()
                    .uri(ollamaApiUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse Ollama response
            if (response != null && response.contains("\"response\":")) {
                int start = response.indexOf("\"response\":\"") + 12;
                int end = response.lastIndexOf("\"");
                if (start > 11 && end > start) {
                    return response.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"");
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("❌ Error calling Ollama: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generate smart approval email
     */
    public String generateSmartApprovalEmail(LeaveRequest leaveRequest, User employee, User hrUser) {
        String prompt = String.format("""
            Write a professional email approving %s's %s leave request from %s to %s (%d days) for %s. 
            Make it warm, include leave details, add preparation reminders, and sign as %s from HR team. 
            Keep it concise and professional. Format as HTML.
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration(),
            leaveRequest.getReason(),
            hrUser.getFullName()
        );

        String aiResponse = generateEmailWithOllama(prompt);
        return aiResponse != null ? aiResponse : generateBasicApprovalEmail(leaveRequest, employee, hrUser);
    }

    /**
     * Generate smart rejection email
     */
    public String generateSmartRejectionEmail(LeaveRequest leaveRequest, User employee, User hrUser, String rejectionReason) {
        String prompt = String.format("""
            Write a professional, empathetic email rejecting %s's %s leave request from %s to %s. 
            The rejection reason is: %s. 
            Be understanding, offer alternatives, encourage discussion. 
            Sign as %s from HR team. Format as HTML.
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            rejectionReason,
            hrUser.getFullName()
        );

        String aiResponse = generateEmailWithOllama(prompt);
        return aiResponse != null ? aiResponse : generateBasicRejectionEmail(leaveRequest, employee, hrUser, rejectionReason);
    }

    // Basic fallback templates
    private String generateBasicApprovalEmail(LeaveRequest leaveRequest, User employee, User hrUser) {
        return String.format("""
            <div style="font-family: Arial, sans-serif;">
                <h2 style="color: green;">✅ Leave Approved</h2>
                <p>Dear %s,</p>
                <p>Your %s leave from %s to %s has been approved.</p>
                <p>Please ensure proper handover before your leave.</p>
                <p>Best regards,<br>%s<br>HR Team</p>
            </div>
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            hrUser.getFullName()
        );
    }

    private String generateBasicRejectionEmail(LeaveRequest leaveRequest, User employee, User hrUser, String rejectionReason) {
        return String.format("""
            <div style="font-family: Arial, sans-serif;">
                <h2 style="color: red;">❌ Leave Request Update</h2>
                <p>Dear %s,</p>
                <p>Your %s leave request has been declined.</p>
                <p><strong>Reason:</strong> %s</p>
                <p>Please feel free to discuss this with HR.</p>
                <p>Best regards,<br>%s<br>HR Team</p>
            </div>
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            rejectionReason,
            hrUser.getFullName()
        );
    }
}