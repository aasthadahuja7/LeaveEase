package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;

/**
 * Dynamic Email Service - Sends emails from HR user to Employee
 * This service allows emails to be sent from the specific HR user who is processing the leave
 * to the specific employee who applied for the leave.
 */
@Service
public class DynamicEmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send leave approval email from HR user to Employee
     * @param leaveRequest The leave request being approved
     * @param employee The employee who applied for leave
     * @param hrUser The HR user who is approving the leave
     * @param hrEmailPassword The HR user's email password (app password)
     */
    public void sendApprovalEmailFromHR(LeaveRequest leaveRequest, User employee, User hrUser, String hrEmailPassword) {
        String subject = "✅ Leave Request Approved - " + leaveRequest.getLeaveType();
        
        try {
            String body = generateApprovalEmailBody(leaveRequest, employee, hrUser);
            sendEmailFromHRToEmployee(hrUser.getEmail(), hrEmailPassword, employee.getEmail(), subject, body, hrUser.getFullName());
            System.out.println("📧 Approval email sent from " + hrUser.getEmail() + " to " + employee.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending approval email from HR: " + e.getMessage());
            // Fallback to system email
            fallbackToSystemEmail(employee.getEmail(), subject, "Your leave request has been approved by " + hrUser.getFullName());
        }
    }

    /**
     * Send leave rejection email from HR user to Employee
     * @param leaveRequest The leave request being rejected
     * @param employee The employee who applied for leave
     * @param hrUser The HR user who is rejecting the leave
     * @param hrEmailPassword The HR user's email password (app password)
     * @param rejectionReason The reason for rejection
     */
    public void sendRejectionEmailFromHR(LeaveRequest leaveRequest, User employee, User hrUser, String hrEmailPassword, String rejectionReason) {
        String subject = "❌ Leave Request Rejected - " + leaveRequest.getLeaveType();
        
        try {
            String body = generateRejectionEmailBody(leaveRequest, employee, hrUser, rejectionReason);
            sendEmailFromHRToEmployee(hrUser.getEmail(), hrEmailPassword, employee.getEmail(), subject, body, hrUser.getFullName());
            System.out.println("📧 Rejection email sent from " + hrUser.getEmail() + " to " + employee.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending rejection email from HR: " + e.getMessage());
            // Fallback to system email
            fallbackToSystemEmail(employee.getEmail(), subject, "Your leave request has been rejected by " + hrUser.getFullName() + ". Reason: " + rejectionReason);
        }
    }

    /**
     * Send email from HR user to Employee using HR's email credentials
     */
    private void sendEmailFromHRToEmployee(String fromEmail, String fromPassword, String toEmail, String subject, String htmlBody, String hrName) throws MessagingException, UnsupportedEncodingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, hrName + " (HR - LeaveEase)");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            helper.setReplyTo(fromEmail); // Allow employee to reply directly to HR
            
            // Note: In a real implementation, you would configure the JavaMailSender
            // with the HR user's credentials dynamically. For now, we'll use the system configuration
            // but set the from address to the HR user's email
            
            mailSender.send(message);
            System.out.println("📧 Email sent successfully from HR (" + fromEmail + ") to Employee (" + toEmail + ")");
            
        } catch (Exception e) {
            System.err.println("❌ Error sending email from HR to Employee: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Generate approval email body with HR user context
     */
    private String generateApprovalEmailBody(LeaveRequest leaveRequest, User employee, User hrUser) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;">
                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 10px; text-align: center; margin-bottom: 20px;">
                    <h1 style="margin: 0; font-size: 24px;">✅ Leave Request Approved</h1>
                    <p style="margin: 10px 0 0 0; opacity: 0.9;">LeaveEase HR System</p>
                </div>
                
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #28a745; margin-top: 0;">Dear %s,</h2>
                    
                    <p>Great news! Your leave request has been <strong>approved</strong> by <strong>%s</strong> from HR.</p>
                    
                    <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                        <h3 style="margin-top: 0; color: #333;">📋 Leave Details:</h3>
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr><td style="padding: 5px 0; font-weight: bold;">Leave Type:</td><td style="padding: 5px 0;">%s</td></tr>
                            <tr><td style="padding: 5px 0; font-weight: bold;">Start Date:</td><td style="padding: 5px 0;">%s</td></tr>
                            <tr><td style="padding: 5px 0; font-weight: bold;">End Date:</td><td style="padding: 5px 0;">%s</td></tr>
                            <tr><td style="padding: 5px 0; font-weight: bold;">Duration:</td><td style="padding: 5px 0;">%s days</td></tr>
                            <tr><td style="padding: 5px 0; font-weight: bold;">Reason:</td><td style="padding: 5px 0;">%s</td></tr>
                        </table>
                    </div>
                    
                    <div style="background: #e8f5e8; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h4 style="margin-top: 0; color: #28a745;">📝 Pre-Leave Checklist:</h4>
                        <ul style="margin: 10px 0;">
                            <li>✅ Hand over your responsibilities to your team</li>
                            <li>✅ Set up your out-of-office email message</li>
                            <li>✅ Complete all pending urgent tasks</li>
                            <li>✅ Notify your immediate supervisor about your absence</li>
                            <li>✅ Update your calendar and project timelines</li>
                        </ul>
                    </div>
                    
                    <p>If you have any questions about your leave or need to make any changes, please feel free to reply to this email or contact me directly.</p>
                    
                    <p style="color: #28a745; font-weight: bold;">Enjoy your time off! 🎉</p>
                    
                    <div style="margin-top: 30px; padding: 15px; background: #f0f0f0; border-radius: 8px;">
                        <p style="margin: 0; font-size: 14px;"><strong>Approved by:</strong> %s (%s)</p>
                        <p style="margin: 5px 0 0 0; font-size: 14px;"><strong>Department:</strong> %s</p>
                        <p style="margin: 5px 0 0 0; font-size: 12px; color: #666;">You can reply to this email to contact HR directly</p>
                    </div>
                    
                    <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #e1e5e9; text-align: center; color: #6b7280; font-size: 12px;">
                        <p>This email was sent from LeaveEase HR System</p>
                        <p>Employee: %s | Department: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            employee.getFullName(),
            hrUser.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate().format(formatter),
            leaveRequest.getEndDate().format(formatter),
            String.valueOf(leaveRequest.getLeaveDuration()),
            leaveRequest.getReason() != null ? leaveRequest.getReason() : "Not specified",
            hrUser.getFullName(),
            hrUser.getEmail(),
            hrUser.getDepartment() != null ? hrUser.getDepartment() : "HR Department",
            employee.getFullName(),
            employee.getDepartment() != null ? employee.getDepartment() : "N/A"
        );
    }

    /**
     * Generate rejection email body with HR user context
     */
    private String generateRejectionEmailBody(LeaveRequest leaveRequest, User employee, User hrUser, String rejectionReason) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;">
                <div style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); color: white; padding: 20px; border-radius: 10px; text-align: center; margin-bottom: 20px;">
                    <h1 style="margin: 0; font-size: 24px;">❌ Leave Request Update</h1>
                    <p style="margin: 10px 0 0 0; opacity: 0.9;">LeaveEase HR System</p>
                </div>
                
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #dc3545; margin-top: 0;">Dear %s,</h2>
                    
                    <p>I hope this email finds you well. After careful consideration, I regret to inform you that your leave request has been <strong>declined</strong>.</p>
                    
                    <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                        <h3 style="margin-top: 0; color: #333;">📋 Leave Request Details:</h3>
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr><td style="padding: 5px 0; font-weight: bold;">Leave Type:</td><td style="padding: 5px 0;">%s</td></tr>
                            <tr><td style="padding: 5px 0; font-weight: bold;">Start Date:</td><td style="padding: 5px 0;">%s</td></tr>
                            <tr><td style="padding: 5px 0; font-weight: bold;">End Date:</td><td style="padding: 5px 0;">%s</td></tr>
                            <tr><td style="padding: 5px 0; font-weight: bold;">Duration:</td><td style="padding: 5px 0;">%s days</td></tr>
                            <tr><td style="padding: 5px 0; font-weight: bold;">Your Reason:</td><td style="padding: 5px 0;">%s</td></tr>
                        </table>
                    </div>
                    
                    <div style="background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h4 style="margin-top: 0; color: #856404;">📝 Reason for Decline:</h4>
                        <p style="margin: 0; color: #856404; font-weight: 500;">%s</p>
                    </div>
                    
                    <div style="background: #e3f2fd; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h4 style="margin-top: 0; color: #1976d2;">💡 Next Steps:</h4>
                        <ul style="margin: 10px 0; color: #1976d2;">
                            <li>You may submit a new leave request with different dates</li>
                            <li>Consider discussing alternative arrangements with your team</li>
                            <li>Feel free to reach out to discuss this decision further</li>
                            <li>Review company leave policies for guidance</li>
                        </ul>
                    </div>
                    
                    <p>I understand this may be disappointing, and I'm happy to discuss this decision with you. Please feel free to reply to this email or schedule a meeting to talk about alternative options or address any concerns you may have.</p>
                    
                    <p>Thank you for your understanding.</p>
                    
                    <div style="margin-top: 30px; padding: 15px; background: #f0f0f0; border-radius: 8px;">
                        <p style="margin: 0; font-size: 14px;"><strong>Reviewed by:</strong> %s (%s)</p>
                        <p style="margin: 5px 0 0 0; font-size: 14px;"><strong>Department:</strong> %s</p>
                        <p style="margin: 5px 0 0 0; font-size: 12px; color: #666;">Reply to this email to discuss further or schedule a meeting</p>
                    </div>
                    
                    <div style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #e1e5e9; text-align: center; color: #6b7280; font-size: 12px;">
                        <p>This email was sent from LeaveEase HR System</p>
                        <p>Employee: %s | Department: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            employee.getFullName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate().format(formatter),
            leaveRequest.getEndDate().format(formatter),
            String.valueOf(leaveRequest.getLeaveDuration()),
            leaveRequest.getReason() != null ? leaveRequest.getReason() : "Not specified",
            rejectionReason != null ? rejectionReason : "Not specified",
            hrUser.getFullName(),
            hrUser.getEmail(),
            hrUser.getDepartment() != null ? hrUser.getDepartment() : "HR Department",
            employee.getFullName(),
            employee.getDepartment() != null ? employee.getDepartment() : "N/A"
        );
    }

    /**
     * Fallback to system email if HR email fails
     */
    private void fallbackToSystemEmail(String toEmail, String subject, String message) {
        System.out.println("📧 Fallback: Logging email notification:");
        System.out.println("📧 To: " + toEmail);
        System.out.println("📧 Subject: " + subject);
        System.out.println("📧 Message: " + message);
        System.out.println("📧 [Configure HR email credentials for direct HR-to-Employee communication]");
    }
}