package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class SmartEmailTemplateService {

    private final Random random = new Random();

    /**
     * Generate personalized approval email with smart content
     */
    public String generateSmartApprovalEmail(LeaveRequest leaveRequest, User employee, User hrUser) {
        String personalizedGreeting = getPersonalizedGreeting(employee.getFullName());
        String leaveTypeMessage = getLeaveTypeSpecificMessage(leaveRequest.getLeaveType(), true);
        String durationMessage = getDurationSpecificMessage((int)calculateLeaveDuration(leaveRequest), true);
        String seasonalMessage = getSeasonalMessage(leaveRequest.getStartDate());
        String preparationTips = getPreparationTips(leaveRequest.getLeaveType(), (int)calculateLeaveDuration(leaveRequest));
        String closingMessage = getPersonalizedClosing(true);

        return String.format("""
            <html>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;">
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 20px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">🎉 Great News!</h1>
                </div>
                
                <div style="padding: 30px; background: white; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <p style="font-size: 18px; margin-bottom: 20px;">%s</p>
                    
                    <div style="background: #f0f8ff; padding: 20px; border-radius: 8px; border-left: 4px solid #4CAF50; margin: 20px 0;">
                        <h2 style="color: #2e7d32; margin-top: 0;">✅ Your Leave Request is Approved!</h2>
                        <p style="margin-bottom: 10px;">%s</p>
                        <p>%s</p>
                    </div>
                    
                    <div style="background: #fafafa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="color: #555; margin-top: 0;">📅 Leave Details</h3>
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr><td style="padding: 8px 0; border-bottom: 1px solid #eee;"><strong>Type:</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #eee;">%s</td></tr>
                            <tr><td style="padding: 8px 0; border-bottom: 1px solid #eee;"><strong>Start Date:</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #eee;">%s</td></tr>
                            <tr><td style="padding: 8px 0; border-bottom: 1px solid #eee;"><strong>End Date:</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #eee;">%s</td></tr>
                            <tr><td style="padding: 8px 0; border-bottom: 1px solid #eee;"><strong>Duration:</strong></td><td style="padding: 8px 0; border-bottom: 1px solid #eee;">%d days</td></tr>
                            <tr><td style="padding: 8px 0;"><strong>Reason:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                        </table>
                    </div>
                    
                    %s
                    
                    <div style="background: #e8f5e8; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <p style="margin: 0; font-style: italic; color: #2e7d32;">%s</p>
                    </div>
                    
                    <p style="margin-top: 30px;">%s</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 2px solid #f0f0f0;">
                        <p style="margin: 0;"><strong>%s</strong><br>
                        <span style="color: #666;">HR Team</span><br>
                        <span style="font-size: 12px; color: #999;">LeaveEase Management System</span></p>
                    </div>
                </div>
            </body>
            </html>
            """,
            personalizedGreeting,
            leaveTypeMessage,
            durationMessage,
            leaveRequest.getLeaveType(),
            formatDate(leaveRequest.getStartDate()),
            formatDate(leaveRequest.getEndDate()),
            (int)calculateLeaveDuration(leaveRequest),
            leaveRequest.getReason(),
            preparationTips,
            seasonalMessage,
            closingMessage,
            hrUser.getFullName()
        );
    }

    /**
     * Generate personalized rejection email with smart content
     */
    public String generateSmartRejectionEmail(LeaveRequest leaveRequest, User employee, User hrUser, String rejectionReason) {
        String personalizedGreeting = getPersonalizedGreeting(employee.getFullName());
        String empathyMessage = getEmpathyMessage(leaveRequest.getLeaveType());
        String alternativeSuggestions = getAlternativeSuggestions(leaveRequest.getLeaveType(), rejectionReason);
        String encouragementMessage = getEncouragementMessage();

        return String.format("""
            <html>
            <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;">
                <div style="background: linear-gradient(135deg, #ff9a9e 0%%, #fecfef 100%%); padding: 20px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: #333; margin: 0; font-size: 24px;">📋 Leave Request Update</h1>
                </div>
                
                <div style="padding: 30px; background: white; border-radius: 0 0 10px 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    <p style="font-size: 18px; margin-bottom: 20px;">%s</p>
                    
                    <p>%s</p>
                    
                    <div style="background: #fff3cd; padding: 20px; border-radius: 8px; border-left: 4px solid #ffc107; margin: 20px 0;">
                        <h3 style="color: #856404; margin-top: 0;">📋 Leave Request Status</h3>
                        <p style="margin-bottom: 10px;">After careful consideration, we need to <strong>decline</strong> your leave request for the following dates:</p>
                        <p><strong>%s to %s</strong> (%d days)</p>
                    </div>
                    
                    <div style="background: #f8d7da; padding: 20px; border-radius: 8px; border-left: 4px solid #dc3545; margin: 20px 0;">
                        <h4 style="color: #721c24; margin-top: 0;">📝 Reason for Decline:</h4>
                        <p style="margin-bottom: 0; font-weight: 500;">%s</p>
                    </div>
                    
                    %s
                    
                    <div style="background: #d1ecf1; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h4 style="color: #0c5460; margin-top: 0;">💡 Next Steps:</h4>
                        <ul style="margin: 0; padding-left: 20px;">
                            <li>Schedule a meeting with HR to discuss alternatives</li>
                            <li>Consider adjusting your leave dates if possible</li>
                            <li>Submit a revised request with different dates</li>
                            <li>Explore partial leave or flexible work arrangements</li>
                        </ul>
                    </div>
                    
                    <p>%s</p>
                    
                    <div style="background: #e2e3e5; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center;">
                        <p style="margin: 0; font-weight: 500; color: #495057;">📞 Need to discuss? Contact HR at hr@company.com or ext. 1234</p>
                    </div>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 2px solid #f0f0f0;">
                        <p style="margin: 0;"><strong>%s</strong><br>
                        <span style="color: #666;">HR Team</span><br>
                        <span style="font-size: 12px; color: #999;">LeaveEase Management System</span></p>
                    </div>
                </div>
            </body>
            </html>
            """,
            personalizedGreeting,
            empathyMessage,
            formatDate(leaveRequest.getStartDate()),
            formatDate(leaveRequest.getEndDate()),
            (int)calculateLeaveDuration(leaveRequest),
            rejectionReason != null ? rejectionReason : "Not specified",
            alternativeSuggestions,
            encouragementMessage,
            hrUser.getFullName()
        );
    }

    // Smart content generation methods
    private String getPersonalizedGreeting(String employeeName) {
        String firstName = employeeName.split(" ")[0];
        List<String> greetings = Arrays.asList(
            "Dear " + firstName + ",",
            "Hi " + firstName + ",",
            "Hello " + firstName + ",",
            "Dear " + employeeName + ","
        );
        return greetings.get(random.nextInt(greetings.size()));
    }

    private String getLeaveTypeSpecificMessage(String leaveType, boolean isApproval) {
        if (isApproval) {
            return switch (leaveType.toLowerCase()) {
                case "annual leave", "vacation" -> "Time to recharge and enjoy some well-deserved rest! 🌴";
                case "sick leave" -> "Take care of your health - that's the most important thing. 🏥";
                case "maternity leave", "paternity leave" -> "Congratulations on this special time with your family! 👶";
                case "emergency leave" -> "We understand the urgency of your situation and support you. 🚨";
                case "study leave" -> "Investing in your education is investing in your future! 📚";
                default -> "We're happy to support your time away from work. ✨";
            };
        } else {
            return "We understand how important this time off is to you, and we appreciate you taking the time to submit your request.";
        }
    }

    private String getDurationSpecificMessage(int duration, boolean isApproval) {
        if (isApproval) {
            if (duration == 1) {
                return "A short break can be just as refreshing! 😊";
            } else if (duration <= 3) {
                return "A few days away will help you come back refreshed and energized! 💪";
            } else if (duration <= 7) {
                return "A week off is perfect for truly disconnecting and recharging! 🔋";
            } else {
                return "An extended break - make the most of this valuable time! 🌟";
            }
        }
        return "";
    }

    private String getSeasonalMessage(LocalDate startDate) {
        try {
            int month = startDate.getMonthValue();
            
            return switch (month) {
                case 12, 1, 2 -> "Perfect timing for some winter relaxation! ❄️";
                case 3, 4, 5 -> "Spring is a wonderful time to take a break! 🌸";
                case 6, 7, 8 -> "Summer vibes - enjoy the sunshine! ☀️";
                case 9, 10, 11 -> "Autumn is beautiful - great choice for time off! 🍂";
                default -> "Enjoy your time away! 🌟";
            };
        } catch (Exception e) {
            return "Enjoy your time away! 🌟";
        }
    }

    private String getPreparationTips(String leaveType, int duration) {
        StringBuilder tips = new StringBuilder();
        tips.append("<div style=\"background: #fff8e1; padding: 20px; border-radius: 8px; margin: 20px 0;\">");
        tips.append("<h4 style=\"color: #f57f17; margin-top: 0;\">📋 Preparation Checklist:</h4>");
        tips.append("<ul style=\"margin: 0; padding-left: 20px;\">");
        
        if (duration >= 3) {
            tips.append("<li>Set up detailed out-of-office email responses</li>");
            tips.append("<li>Brief your team about ongoing projects and deadlines</li>");
            tips.append("<li>Prepare comprehensive handover documentation</li>");
        } else {
            tips.append("<li>Set up out-of-office email responses</li>");
            tips.append("<li>Inform your immediate team about your absence</li>");
        }
        
        if (leaveType.toLowerCase().contains("sick")) {
            tips.append("<li>Focus on your recovery - work can wait</li>");
            tips.append("<li>Keep HR updated on your expected return date</li>");
        } else {
            tips.append("<li>Complete urgent tasks before your leave starts</li>");
            tips.append("<li>Update project status and timelines</li>");
        }
        
        tips.append("</ul></div>");
        return tips.toString();
    }

    private String getPersonalizedClosing(boolean isApproval) {
        if (isApproval) {
            List<String> closings = Arrays.asList(
                "We hope you have a wonderful and refreshing time off!",
                "Enjoy every moment of your well-deserved break!",
                "Take care and see you when you return, refreshed and ready!",
                "Have an amazing time away - you've earned it!"
            );
            return closings.get(random.nextInt(closings.size()));
        } else {
            return "Thank you for your understanding, and we look forward to working with you to find a suitable solution.";
        }
    }

    private String getEmpathyMessage(String leaveType) {
        return switch (leaveType.toLowerCase()) {
            case "sick leave" -> "Your health and well-being are our top priority, and we want to ensure you get the care you need.";
            case "emergency leave" -> "We understand that emergencies require immediate attention and can be stressful situations.";
            case "maternity leave", "paternity leave" -> "We recognize how important this time is for you and your growing family.";
            default -> "We truly appreciate you taking the time to plan ahead and submit your leave request.";
        };
    }

    private String getAlternativeSuggestions(String leaveType, String rejectionReason) {
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("<div style=\"background: #e8f4fd; padding: 20px; border-radius: 8px; margin: 20px 0;\">");
        suggestions.append("<h4 style=\"color: #1976d2; margin-top: 0;\">💡 Alternative Options to Consider:</h4>");
        suggestions.append("<ul style=\"margin: 0; padding-left: 20px;\">");
        
        if (rejectionReason != null && rejectionReason.toLowerCase().contains("busy period")) {
            suggestions.append("<li>Consider dates outside our peak business period</li>");
            suggestions.append("<li>Explore splitting your leave into shorter periods</li>");
        } else if (rejectionReason != null && rejectionReason.toLowerCase().contains("staffing")) {
            suggestions.append("<li>Coordinate with team members for coverage</li>");
            suggestions.append("<li>Consider a different time when more staff is available</li>");
        } else {
            suggestions.append("<li>Flexible work arrangements or remote work options</li>");
            suggestions.append("<li>Partial leave or reduced hours during the requested period</li>");
        }
        
        suggestions.append("<li>Discuss with your manager about project timelines</li>");
        suggestions.append("<li>Consider alternative dates that work better for the team</li>");
        suggestions.append("</ul></div>");
        
        return suggestions.toString();
    }

    private String getEncouragementMessage() {
        List<String> messages = Arrays.asList(
            "We value you as an employee and want to work together to find a solution that works for everyone.",
            "Your well-being is important to us, and we're committed to finding a way to accommodate your needs.",
            "We appreciate your flexibility and understanding as we work through this together.",
            "Let's collaborate to find the best possible outcome for both you and the team."
        );
        return messages.get(random.nextInt(messages.size()));
    }

    private String formatDate(LocalDate date) {
        try {
            return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        } catch (Exception e) {
            return date.toString();
        }
    }
    
    private long calculateLeaveDuration(LeaveRequest leaveRequest) {
        try {
            return ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        } catch (Exception e) {
            return 1;
        }
    }
}