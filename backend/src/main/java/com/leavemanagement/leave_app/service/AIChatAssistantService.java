package com.leavemanagement.leave_app.service;

import com.leavemanagement.leave_app.model.LeaveRequest;
import com.leavemanagement.leave_app.model.User;
import com.leavemanagement.leave_app.repository.LeaveRequestRepository;
import com.leavemanagement.leave_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AIChatAssistantService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserRepository userRepository;

    public String processChatMessage(String message, String username) {
        try {
            String lowerMessage = message.toLowerCase();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                return "I'm sorry, I couldn't find your user profile. Please contact HR.";
            }

            User user = userOpt.get();

            if (lowerMessage.contains("leave") || lowerMessage.contains("vacation")) {
                return handleLeaveQuery(lowerMessage, user);
            }
            if (lowerMessage.contains("absent") || lowerMessage.contains("who is not here")) {
                return handleAbsenceQuery(user);
            }
            if (lowerMessage.contains("balance") || lowerMessage.contains("remaining")) {
                return handleLeaveBalanceQuery(user);
            }
            if (lowerMessage.contains("help") || lowerMessage.contains("what can you do")) {
                return getHelpMessage();
            }

            return getDefaultResponse();
        } catch (Exception e) {
            return "I'm sorry, I encountered an error. Please try again or contact HR.";
        }
    }

    private String handleLeaveQuery(String message, User user) {
        if (message.contains("my leave") || message.contains("my vacation")) {
            return getMyLeaveStatus(user);
        }
        if (message.contains("pending") || message.contains("waiting")) {
            return getPendingLeaves(user);
        }
        if (message.contains("approved") || message.contains("confirmed")) {
            return getApprovedLeaves(user);
        }
        if (message.contains("rejected") || message.contains("denied")) {
            return getRejectedLeaves(user);
        }

        return "I can help you with your leave information. You can ask about:\n" +
                "• My leave status\n" +
                "• Pending leave requests\n" +
                "• Approved leaves\n" +
                "• Leave balance\n" +
                "• Who is absent today";
    }

    private String handleAbsenceQuery(User user) {
        List<LeaveRequest> todayLeaves = leaveRequestRepository.findCurrentlyOnLeave(LocalDate.now());

        if (todayLeaves.isEmpty()) {
            return "Everyone is present today! 🎉";
        }

        StringBuilder response = new StringBuilder("People on leave today:\n");
        todayLeaves.forEach(leave -> response.append("• ")
                .append(leave.getEmployeeName())
                .append(" (")
                .append(leave.getLeaveType())
                .append(")\n"));

        return response.toString();
    }

    private String handleLeaveBalanceQuery(User user) {
        List<LeaveRequest> userLeaves = leaveRequestRepository.findByEmployeeName(user.getFullName());
        long usedLeaves = userLeaves.stream()
                .filter(leave -> "Approved".equals(leave.getStatus()))
                .mapToLong(LeaveRequest::getLeaveDuration)
                .sum();

        long totalEntitlement = 25;
        long remainingLeaves = totalEntitlement - usedLeaves;

        return String.format("Your leave balance:\n" +
                        "• Total entitlement: %d days\n" +
                        "• Used: %d days\n" +
                        "• Remaining: %d days",
                totalEntitlement, usedLeaves, remainingLeaves);
    }

    private String getMyLeaveStatus(User user) {
        List<LeaveRequest> userLeaves = leaveRequestRepository.findByEmployeeName(user.getFullName());

        if (userLeaves.isEmpty()) {
            return "You haven't submitted any leave requests yet.";
        }

        List<LeaveRequest> recentLeaves = userLeaves.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());

        return buildLeaveSummary("Your recent leave requests:\n", recentLeaves, false);
    }

    private String getPendingLeaves(User user) {
        List<LeaveRequest> pendingLeaves = getLeavesByStatus(user, "Pending");

        if (pendingLeaves.isEmpty()) {
            return "You have no pending leave requests.";
        }

        return buildLeaveSummary("Your pending leave requests:\n", pendingLeaves, true);
    }

    private String getApprovedLeaves(User user) {
        List<LeaveRequest> approvedLeaves = getLeavesByStatus(user, "Approved");

        if (approvedLeaves.isEmpty()) {
            return "You have no approved leave requests.";
        }

        return buildLeaveSummary("Your approved leave requests:\n", approvedLeaves, true);
    }

    private String getRejectedLeaves(User user) {
        List<LeaveRequest> rejectedLeaves = getLeavesByStatus(user, "Rejected");

        if (rejectedLeaves.isEmpty()) {
            return "You have no rejected leave requests.";
        }

        StringBuilder response = new StringBuilder("Your rejected leave requests:\n");
        for (LeaveRequest leave : rejectedLeaves) {
            response.append("• ")
                    .append(leave.getLeaveType())
                    .append(": ")
                    .append(leave.getStartDate().format(DATE_FORMATTER))
                    .append(" to ")
                    .append(leave.getEndDate().format(DATE_FORMATTER))
                    .append(" - Reason: ")
                    .append(leave.getRejectionReason() != null ? leave.getRejectionReason() : "Not specified")
                    .append("\n");
        }

        return response.toString();
    }

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

    private String getDefaultResponse() {
        return "I'm your AI HR Assistant! I can help you with leave information, " +
                "team absences, and more. Try asking:\n" +
                "• \"My leave status\"\n" +
                "• \"Who is absent today\"\n" +
                "• \"Leave balance\"\n" +
                "• \"Help\" for more options";
    }

    private List<LeaveRequest> getLeavesByStatus(User user, String status) {
        return leaveRequestRepository.findByEmployeeName(user.getFullName()).stream()
                .filter(leave -> status.equals(leave.getStatus()))
                .collect(Collectors.toList());
    }

    private String buildLeaveSummary(String title, List<LeaveRequest> leaves, boolean includeDuration) {
        StringBuilder response = new StringBuilder(title);

        for (LeaveRequest leave : leaves) {
            response.append("• ")
                    .append(leave.getLeaveType())
                    .append(": ")
                    .append(leave.getStartDate().format(DATE_FORMATTER))
                    .append(" to ")
                    .append(leave.getEndDate().format(DATE_FORMATTER));

            if (includeDuration) {
                response.append(" (")
                        .append(leave.getLeaveDuration())
                        .append(" days)");
            } else {
                response.append(" - ")
                        .append(leave.getStatus());
            }

            response.append("\n");
        }

        return response.toString();
    }
} 