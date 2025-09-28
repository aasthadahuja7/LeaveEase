package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import com.leavemanagment.leave_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class HREmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Value("${spring.mail.username:}")
    private String systemEmail;

    @Value("${spring.mail.password:}")
    private String systemPassword;

    /**
     * Send leave approval email from HR's email account
     */
    public void sendLeaveApprovedEmailFromHR(LeaveRequest leaveRequest, User employee, Authentication hrAuthentication) {
        try {
            // Get HR user details
            User hrUser = getHRUser(hrAuthentication);
            if (hrUser == null) {
                System.err.println("❌ HR user not found for email sending");
                return;
            }

            String subject = "✅ Leave Request Approved - " + leaveRequest.getLeaveType();
            String htmlBody = generateHRApprovalEmailBody(leaveRequest, employee, hrUser);
            
            sendHtmlEmailFromHR(employee.getEmail(), subject, htmlBody, hrUser);
            System.out.println("📧 HR approval email sent from: " + hrUser.getEmail() + " to: " + employee.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending HR approval email: " + e.getMessage());
        }
    }

    /**
     * Send leave rejection email from HR's email account
     */
    public void sendLeaveRejectedEmailFromHR(LeaveRequest leaveRequest, User employee, String rejectionReason, Authentication hrAuthentication) {
        try {
            // Get HR user details
            User hrUser = getHRUser(hrAuthentication);
            if (hrUser == null) {
                System.err.println("❌ HR user not found for email sending");
                return;
            }

            String subject = "❌ Leave Request Rejected - " + leaveRequest.getLeaveType();
            String htmlBody = generateHRRejectionEmailBody(leaveRequest, employee, rejectionReason, hrUser);
            
            sendHtmlEmailFromHR(employee.getEmail(), subject, htmlBody, hrUser);
            System.out.println("📧 HR rejection email sent from: " + hrUser.getEmail() + " to: " + employee.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending HR rejection email: " + e.getMessage());
        }
    }

    /**
     * Get HR user from authentication
     */
    private User getHRUser(Authentication authentication) {
        if (authentication == null) {
            System.err.println("❌ No authentication provided");
            return null;
        }

        String hrUsername = authentication.getName();
        System.out.println("👤 HR Username: " + hrUsername);

        // Find HR user by username
        return userRepository.findByUsername(hrUsername).orElse(null);
    }

    /**
     * Generate approval email body from HR's perspective
     */
    private String generateHRApprovalEmailBody(LeaveRequest leaveRequest, User employee, User hrUser) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;">
                <div style="background: linear-gradient(135deg, #28a745 0%, #20c997 100%); color: white; padding: 20px; border-radius: 10px; text-align: center; margin-bottom: 20px;">
                    <h1 style="margin: 0; font-size: 24px;">✅ Leave Request Approved</h1>
                    <p style="margin: 10px 0 0 0; opacity: 0.9;">LeaveEase HR System</p>
                </div>
                
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #28a745; margin-top: 0;">Dear %s,</h2>
                    
                    <p>I am pleased to inform you that your leave request has been <strong>approved</strong>.</p>
                    
                    <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745;">
                        <h3 style="margin-top: 0; color: #333;">Leave Details:</h3>
                        <ul style="list-style: none; padding: 0;">
                            <li><strong>Leave Type:</strong> %s</li>
                            <li><strong>Start Date:</strong> %s</li>
                            <li><strong>End Date:</strong> %s</li>
                            <li><strong>Duration:</strong> %s days</li>
                            <li><strong>Reason:</strong> %s</li>
                        </ul>
                    </div>
                    
                    <p>Please ensure you have:</p>
                    <ul>
                        <li>Handed over your responsibilities to your team</li>
                        <li>Updated your out-of-office message</li>
                        <li>Notified your immediate supervisor</li>
                    </ul>
                    
                    <p>If you have any questions, please don't hesitate to contact me.</p>
                    
                    <p>Enjoy your time off! 🎉</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e1e5e9; text-align: center; color: #6b7280; font-size: 12px;">
                        <p>Best regards,</p>
                        <p><strong>%s</strong></p>
                        <p>HR Department</p>
                        <p>Email: %s</p>
                        <p>This is an automated message from LeaveEase HR System</p>
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
            hrUser.getFullName(),
            hrUser.getEmail()
        );
    }

    /**
     * Generate rejection email body from HR's perspective
     */
    private String generateHRRejectionEmailBody(LeaveRequest leaveRequest, User employee, String rejectionReason, User hrUser) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;">
                <div style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); color: white; padding: 20px; border-radius: 10px; text-align: center; margin-bottom: 20px;">
                    <h1 style="margin: 0; font-size: 24px;">❌ Leave Request Rejected</h1>
                    <p style="margin: 10px 0 0 0; opacity: 0.9;">LeaveEase HR System</p>
                </div>
                
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #dc3545; margin-top: 0;">Dear %s,</h2>
                    
                    <p>I regret to inform you that your leave request has been <strong>rejected</strong>.</p>
                    
                    <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc3545;">
                        <h3 style="margin-top: 0; color: #333;">Leave Details:</h3>
                        <ul style="list-style: none; padding: 0;">
                            <li><strong>Leave Type:</strong> %s</li>
                            <li><strong>Start Date:</strong> %s</li>
                            <li><strong>End Date:</strong> %s</li>
                            <li><strong>Duration:</strong> %s days</li>
                            <li><strong>Reason:</strong> %s</li>
                        </ul>
                    </div>
                    
                    <div style="background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <h4 style="margin-top: 0; color: #856404;">Rejection Reason:</h4>
                        <p style="margin: 0; color: #856404;">%s</p>
                    </div>
                    
                    <p>If you believe this decision was made in error or if you have any questions, please contact me for clarification.</p>
                    
                    <p>You may submit a new leave request with appropriate modifications.</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e1e5e9; text-align: center; color: #6b7280; font-size: 12px;">
                        <p>Best regards,</p>
                        <p><strong>%s</strong></p>
                        <p>HR Department</p>
                        <p>Email: %s</p>
                        <p>This is an automated message from LeaveEase HR System</p>
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
            hrUser.getEmail()
        );
    }

    /**
     * Send HTML email from HR's email account
     */
    private void sendHtmlEmailFromHR(String toEmail, String subject, String htmlBody, User hrUser) throws MessagingException {
        if (systemEmail.isEmpty() || systemPassword.isEmpty()) {
            System.out.println("📧 Email configuration not set up. Logging notification instead:");
            System.out.println("📧 From: " + hrUser.getEmail() + " (HR's email)");
            System.out.println("📧 To: " + toEmail);
            System.out.println("📧 Subject: " + subject);
            System.out.println("📧 Body: " + htmlBody.substring(0, Math.min(200, htmlBody.length())) + "...");
            System.out.println("📧 [Please configure email settings in application.properties]");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set the "from" address to show the HR's email
            helper.setFrom(systemEmail); // System email for SMTP authentication
            helper.setReplyTo(hrUser.getEmail()); // Reply-to shows HR's email
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            
            mailSender.send(message);
            System.out.println("📧 HR email sent successfully from: " + hrUser.getEmail() + " to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Error sending HR email: " + e.getMessage());
            // Fallback to simple email
            sendSimpleEmailFromHR(toEmail, subject, htmlBody.replaceAll("<[^>]*>", ""), hrUser);
        }
    }

    /**
     * Send simple text email from HR's email account (fallback)
     */
    private void sendSimpleEmailFromHR(String toEmail, String subject, String body, User hrUser) {
        if (systemEmail.isEmpty() || systemPassword.isEmpty()) {
            System.out.println("📧 Email configuration not set up. Logging notification instead:");
            System.out.println("📧 From: " + hrUser.getEmail() + " (HR's email)");
            System.out.println("📧 To: " + toEmail);
            System.out.println("📧 Subject: " + subject);
            System.out.println("📧 Body: " + body.substring(0, Math.min(200, body.length())) + "...");
            System.out.println("📧 [Please configure email settings in application.properties]");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(systemEmail); // System email for SMTP authentication
            message.setReplyTo(hrUser.getEmail()); // Reply-to shows HR's email
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            System.out.println("📧 Simple HR email sent successfully from: " + hrUser.getEmail() + " to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending simple HR email: " + e.getMessage());
        }
    }

    /**
     * Test HR email configuration
     */
    public boolean testHREmailConfiguration(Authentication authentication) {
        try {
            if (systemEmail.isEmpty() || systemPassword.isEmpty()) {
                System.out.println("📧 HR email configuration not set up");
                return false;
            }

            // Get HR user
            User hrUser = getHRUser(authentication);
            if (hrUser == null) {
                System.out.println("❌ HR user not found for testing");
                return false;
            }

            String testSubject = "Test HR Email";
            String testBody = "This is a test email from HR perspective.";

            sendSimpleEmailFromHR("test@example.com", testSubject, testBody, hrUser);
            return true;
        } catch (Exception e) {
            System.err.println("❌ HR email test failed: " + e.getMessage());
            return false;
        }
    }
}
