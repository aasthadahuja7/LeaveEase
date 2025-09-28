package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import com.leavemanagment.leave_app.repository.LeaveRequestRepository;
import com.leavemanagment.leave_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AIChatAssistantService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Process AI chat message and return response
     */
    public String processChatMessage(String message, String username) {
        try {
            String lowerMessage = message.toLowerCase();
            
            // Get user context
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (!userOpt.isPresent()) {
                return "I'm sorry, I couldn't find your user profile. Please contact HR.";
            }
            
            User user = userOpt.get();
            
            // Process different types of queries
            if (lowerMessage.contains("leave") || lowerMessage.contains("vacation")) {
                return handleLeaveQuery(lowerMessage, user);
            } else if (lowerMessage.contains("absent") || lowerMessage.contains("who is not here")) {
                return handleAbsenceQuery(lowerMessage, user);
            } else if (lowerMessage.contains("balance") || lowerMessage.contains("remaining")) {
                return handleLeaveBalanceQuery(user);
            } else if (lowerMessage.contains("help") || lowerMessage.contains("what can you do")) {
                return getHelpMessage();
            } else {
                return getDefaultResponse();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing AI chat message: " + e.getMessage());
            return "I'm sorry, I encountered an error. Please try again or contact HR.";
        }
    }

    /**
     * Handle leave-related queries
     */
    private String handleLeaveQuery(String message, User user) {
        if (message.contains("my leave") || message.contains("my vacation")) {
            return getMyLeaveStatus(user);
        } else if (message.contains("pending") || message.contains("waiting")) {
            return getPendingLeaves(user);
        } else if (message.contains("approved") || message.contains("confirmed")) {
            return getApprovedLeaves(user);
        } else if (message.contains("rejected") || message.contains("denied")) {
            return getRejectedLeaves(user);
        } else {
            return "I can help you with your leave information. You can ask about:\n" +
                   "• My leave status\n" +
                   "• Pending leave requests\n" +
                   "• Approved leaves\n" +
                   "• Leave balance\n" +
                   "• Who is absent today";
        }
    }

    /**
     * Handle absence queries
     */
    private String handleAbsenceQuery(String message, User user) {
        LocalDate today = LocalDate.now();
        List<LeaveRequest> todayLeaves = leaveRequestRepository.findCurrentlyOnLeave(today);
        
        if (todayLeaves.isEmpty()) {
            return "Everyone is present today! 🎉";
        }
        
        StringBuilder response = new StringBuilder("People on leave today:\n");
        for (LeaveRequest leave : todayLeaves) {
            response.append("• ").append(leave.getEmployeeName())
                   .append(" (").append(leave.getLeaveType()).append(")\n");
        }
        
        return response.toString();
    }

    /**
     * Handle leave balance queries
     */
    private String handleLeaveBalanceQuery(User user) {
        // Calculate leave balance (simplified)
        List<LeaveRequest> userLeaves = leaveRequestRepository.findByEmployeeName(user.getFullName());
        long usedLeaves = userLeaves.stream()
                .filter(leave -> "Approved".equals(leave.getStatus()))
                .mapToLong(LeaveRequest::getLeaveDuration)
                .sum();
        
        long totalEntitlement = 25; // Assuming 25 days annual leave
        long remainingLeaves = totalEntitlement - usedLeaves;
        
        return String.format("Your leave balance:\n" +
                           "• Total entitlement: %d days\n" +
                           "• Used: %d days\n" +
                           "• Remaining: %d days", 
                           totalEntitlement, usedLeaves, remainingLeaves);
    }

    /**
     * Get user's leave status
     */
    private String getMyLeaveStatus(User user) {
        List<LeaveRequest> userLeaves = leaveRequestRepository.findByEmployeeName(user.getFullName());
        
        if (userLeaves.isEmpty()) {
            return "You haven't submitted any leave requests yet.";
        }
        
        // Get recent leaves
        List<LeaveRequest> recentLeaves = userLeaves.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());
        
        StringBuilder response = new StringBuilder("Your recent leave requests:\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        for (LeaveRequest leave : recentLeaves) {
            response.append("• ").append(leave.getLeaveType())
                   .append(": ").append(leave.getStartDate().format(formatter))
                   .append(" to ").append(leave.getEndDate().format(formatter))
                   .append(" - ").append(leave.getStatus())
                   .append("\n");
        }
        
        return response.toString();
    }

    /**
     * Get pending leaves for user
     */
    private String getPendingLeaves(User user) {
        List<LeaveRequest> pendingLeaves = leaveRequestRepository.findByEmployeeName(user.getFullName())
                .stream()
                .filter(leave -> "Pending".equals(leave.getStatus()))
                .collect(Collectors.toList());
        
        if (pendingLeaves.isEmpty()) {
            return "You have no pending leave requests.";
        }
        
        StringBuilder response = new StringBuilder("Your pending leave requests:\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        for (LeaveRequest leave : pendingLeaves) {
            response.append("• ").append(leave.getLeaveType())
                   .append(": ").append(leave.getStartDate().format(formatter))
                   .append(" to ").append(leave.getEndDate().format(formatter))
                   .append(" (").append(leave.getLeaveDuration()).append(" days)\n");
        }
        
        return response.toString();
    }

    /**
     * Get approved leaves for user
     */
    private String getApprovedLeaves(User user) {
        List<LeaveRequest> approvedLeaves = leaveRequestRepository.findByEmployeeName(user.getFullName())
                .stream()
                .filter(leave -> "Approved".equals(leave.getStatus()))
                .collect(Collectors.toList());
        
        if (approvedLeaves.isEmpty()) {
            return "You have no approved leave requests.";
        }
        
        StringBuilder response = new StringBuilder("Your approved leave requests:\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        for (LeaveRequest leave : approvedLeaves) {
            response.append("• ").append(leave.getLeaveType())
                   .append(": ").append(leave.getStartDate().format(formatter))
                   .append(" to ").append(leave.getEndDate().format(formatter))
                   .append(" (").append(leave.getLeaveDuration()).append(" days)\n");
        }
        
        return response.toString();
    }

    /**
     * Get rejected leaves for user
     */
    private String getRejectedLeaves(User user) {
        List<LeaveRequest> rejectedLeaves = leaveRequestRepository.findByEmployeeName(user.getFullName())
                .stream()
                .filter(leave -> "Rejected".equals(leave.getStatus()))
                .collect(Collectors.toList());
        
        if (rejectedLeaves.isEmpty()) {
            return "You have no rejected leave requests.";
        }
        
        StringBuilder response = new StringBuilder("Your rejected leave requests:\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        for (LeaveRequest leave : rejectedLeaves) {
            response.append("• ").append(leave.getLeaveType())
                   .append(": ").append(leave.getStartDate().format(formatter))
                   .append(" to ").append(leave.getEndDate().format(formatter))
                   .append(" - Reason: ").append(leave.getRejectionReason() != null ? leave.getRejectionReason() : "Not specified")
                   .append("\n");
        }
        
        return response.toString();
    }

    /**
     * Get help message
     */
    private String getHelpMessage() {
        return "🤖 I'm your AI HR Assistant! I can help you with:\n\n" +
               "📅 **Leave Information:**\n" +
               "• \"My leave status\" - Check your leave requests\n" +
               "• \"Pending leaves\" - View pending requests\n" +
               "• \"Approved leaves\" - View approved requests\n" +
               "• \"Leave balance\" - Check remaining days\n\n" +
               "👥 **Team Information:**\n" +
               "• \"Who is absent today\" - See who's on leave\n" +
               "• \"Who is not here\" - Check team absences\n\n" +
               "💡 **Other:**\n" +
               "• \"Help\" - Show this message\n\n" +
               "Just ask me anything about your leave or team!";
    }

    /**
     * Get default response
     */
    private String getDefaultResponse() {
        return "I'm your AI HR Assistant! I can help you with leave information, " +
               "team absences, and more. Try asking:\n" +
               "• \"My leave status\"\n" +
               "• \"Who is absent today\"\n" +
               "• \"Leave balance\"\n" +
               "• \"Help\" for more options";
    }
} 