package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIEmailGeneratorService {

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    private final WebClient webClient;

    public AIEmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Generate AI-powered approval email content
     */
    public String generateApprovalEmail(LeaveRequest leaveRequest, User employee, User hrUser) {
        try {
            String prompt = createApprovalPrompt(leaveRequest, employee, hrUser);
            return callOpenAI(prompt);
        } catch (Exception e) {
            System.err.println("❌ Error generating AI approval email: " + e.getMessage());
            return generateFallbackApprovalEmail(leaveRequest, employee, hrUser);
        }
    }

    /**
     * Generate AI-powered rejection email content
     */
    public String generateRejectionEmail(LeaveRequest leaveRequest, User employee, User hrUser, String rejectionReason) {
        try {
            String prompt = createRejectionPrompt(leaveRequest, employee, hrUser, rejectionReason);
            return callOpenAI(prompt);
        } catch (Exception e) {
            System.err.println("❌ Error generating AI rejection email: " + e.getMessage());
            return generateFallbackRejectionEmail(leaveRequest, employee, hrUser, rejectionReason);
        }
    }

    /**
     * Generate AI-powered reminder email content
     */
    public String generateReminderEmail(LeaveRequest leaveRequest, User employee) {
        try {
            String prompt = createReminderPrompt(leaveRequest, employee);
            return callOpenAI(prompt);
        } catch (Exception e) {
            System.err.println("❌ Error generating AI reminder email: " + e.getMessage());
            return generateFallbackReminderEmail(leaveRequest, employee);
        }
    }

    /**
     * Create prompt for approval email
     */
    private String createApprovalPrompt(LeaveRequest leaveRequest, User employee, User hrUser) {
        return String.format("""
            Generate a professional, warm, and personalized email for approving a leave request.
            
            Context:
            - Employee: %s (%s)
            - HR Manager: %s
            - Leave Type: %s
            - Start Date: %s
            - End Date: %s
            - Duration: %d days
            - Reason: %s
            
            Requirements:
            - Professional but friendly tone
            - Include all leave details
            - Add helpful reminders about handover
            - Wish them well for their time off
            - Keep it concise but personal
            - Use appropriate emojis sparingly
            - Sign off as HR team
            
            Format as HTML email body.
            """, 
            employee.getFullName(), employee.getEmail(),
            hrUser.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration(),
            leaveRequest.getReason()
        );
    }

    /**
     * Create prompt for rejection email
     */
    private String createRejectionPrompt(LeaveRequest leaveRequest, User employee, User hrUser, String rejectionReason) {
        return String.format("""
            Generate a professional, empathetic email for rejecting a leave request.
            
            Context:
            - Employee: %s (%s)
            - HR Manager: %s
            - Leave Type: %s
            - Start Date: %s
            - End Date: %s
            - Duration: %d days
            - Employee's Reason: %s
            - Rejection Reason: %s
            
            Requirements:
            - Empathetic and understanding tone
            - Clearly explain the rejection reason
            - Offer alternatives or suggestions if possible
            - Encourage them to discuss or reapply
            - Maintain positive relationship
            - Be respectful and professional
            - Sign off as HR team
            
            Format as HTML email body.
            """, 
            employee.getFullName(), employee.getEmail(),
            hrUser.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration(),
            leaveRequest.getReason(),
            rejectionReason != null ? rejectionReason : "Not specified"
        );
    }

    /**
     * Create prompt for reminder email
     */
    private String createReminderPrompt(LeaveRequest leaveRequest, User employee) {
        return String.format("""
            Generate a friendly reminder email for upcoming approved leave.
            
            Context:
            - Employee: %s
            - Leave Type: %s
            - Start Date: %s
            - End Date: %s
            - Duration: %d days
            
            Requirements:
            - Friendly and helpful tone
            - Remind about upcoming leave
            - Include checklist for preparation
            - Mention handover responsibilities
            - Wish them well
            - Keep it brief and actionable
            
            Format as HTML email body.
            """, 
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration()
        );
    }

    /**
     * Call OpenAI API to generate email content
     */
    private String callOpenAI(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", new Object[]{
                Map.of("role", "user", "content", prompt)
            });
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);

            String response = webClient.post()
                    .uri(openaiApiUrl)
                    .header("Authorization", "Bearer " + openaiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse the response and extract the generated content
            // This is a simplified version - you might want to use a JSON library
            if (response != null && response.contains("content")) {
                int start = response.indexOf("\"content\":\"") + 11;
                int end = response.indexOf("\"", start);
                if (start > 10 && end > start) {
                    return response.substring(start, end)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"");
                }
            }

            System.err.println("❌ Unexpected OpenAI response format");
            return null;

        } catch (Exception e) {
            System.err.println("❌ Error calling OpenAI API: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fallback approval email template
     */
    private String generateFallbackApprovalEmail(LeaveRequest leaveRequest, User employee, User hrUser) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #28a745;">✅ Leave Request Approved</h2>
                    
                    <p>Dear %s,</p>
                    
                    <p>Great news! Your leave request has been <strong>approved</strong>.</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #495057;">📅 Leave Details:</h3>
                        <ul style="list-style: none; padding: 0;">
                            <li><strong>Type:</strong> %s</li>
                            <li><strong>Start Date:</strong> %s</li>
                            <li><strong>End Date:</strong> %s</li>
                            <li><strong>Duration:</strong> %d days</li>
                            <li><strong>Reason:</strong> %s</li>
                        </ul>
                    </div>
                    
                    <p><strong>📋 Before your leave:</strong></p>
                    <ul>
                        <li>Complete pending tasks and projects</li>
                        <li>Brief your team about ongoing work</li>
                        <li>Set up out-of-office email responses</li>
                        <li>Ensure proper handover documentation</li>
                    </ul>
                    
                    <p>Have a wonderful time off! 🌟</p>
                    
                    <p>Best regards,<br>
                    <strong>%s</strong><br>
                    HR Team</p>
                </div>
            </body>
            </html>
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration(),
            leaveRequest.getReason(),
            hrUser.getFullName()
        );
    }

    /**
     * Fallback rejection email template
     */
    private String generateFallbackRejectionEmail(LeaveRequest leaveRequest, User employee, User hrUser, String rejectionReason) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #dc3545;">❌ Leave Request Update</h2>
                    
                    <p>Dear %s,</p>
                    
                    <p>Thank you for submitting your leave request. After careful consideration, we regret to inform you that your request has been <strong>declined</strong>.</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #495057;">📅 Leave Details:</h3>
                        <ul style="list-style: none; padding: 0;">
                            <li><strong>Type:</strong> %s</li>
                            <li><strong>Start Date:</strong> %s</li>
                            <li><strong>End Date:</strong> %s</li>
                            <li><strong>Duration:</strong> %d days</li>
                            <li><strong>Your Reason:</strong> %s</li>
                        </ul>
                    </div>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; border-left: 4px solid #ffc107;">
                        <h4 style="margin-top: 0; color: #856404;">📋 Reason for Decline:</h4>
                        <p style="margin-bottom: 0;">%s</p>
                    </div>
                    
                    <p><strong>Next Steps:</strong></p>
                    <ul>
                        <li>Feel free to discuss this decision with HR</li>
                        <li>Consider alternative dates if possible</li>
                        <li>Submit a revised request if circumstances change</li>
                    </ul>
                    
                    <p>We appreciate your understanding and look forward to working with you on finding a suitable solution.</p>
                    
                    <p>Best regards,<br>
                    <strong>%s</strong><br>
                    HR Team</p>
                </div>
            </body>
            </html>
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration(),
            leaveRequest.getReason(),
            rejectionReason != null ? rejectionReason : "Not specified",
            hrUser.getFullName()
        );
    }

    /**
     * Fallback reminder email template
     */
    private String generateFallbackReminderEmail(LeaveRequest leaveRequest, User employee) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #17a2b8;">🔔 Leave Reminder</h2>
                    
                    <p>Dear %s,</p>
                    
                    <p>This is a friendly reminder about your upcoming approved leave.</p>
                    
                    <div style="background-color: #e7f3ff; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #0c5460;">📅 Leave Details:</h3>
                        <ul style="list-style: none; padding: 0;">
                            <li><strong>Type:</strong> %s</li>
                            <li><strong>Start Date:</strong> %s</li>
                            <li><strong>End Date:</strong> %s</li>
                            <li><strong>Duration:</strong> %d days</li>
                        </ul>
                    </div>
                    
                    <p><strong>📋 Preparation Checklist:</strong></p>
                    <ul>
                        <li>✅ Complete urgent tasks</li>
                        <li>✅ Brief team members</li>
                        <li>✅ Set out-of-office messages</li>
                        <li>✅ Prepare handover notes</li>
                        <li>✅ Update project status</li>
                    </ul>
                    
                    <p>Enjoy your well-deserved break! 🌴</p>
                    
                    <p>Best regards,<br>
                    HR Team</p>
                </div>
            </body>
            </html>
            """,
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            leaveRequest.getLeaveDuration()
        );
    }
}