package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.LeaveRequest;
import com.leavemanagment.leave_app.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.password:}")
    private String emailPassword;

    /**
     * Send leave approval email
     */
    public void sendLeaveApprovedEmail(LeaveRequest leaveRequest, User employee) {
        String subject = "✅ Leave Request Approved - " + leaveRequest.getLeaveType();
        
        try {
            String body = generateApprovalEmailBody(leaveRequest, employee);
            sendHtmlEmail(employee.getEmail(), subject, body);
            System.out.println("📧 Email sent: Leave approved for " + employee.getFullName());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending approval email: " + e.getMessage());
            // Fallback to simple text email
            String simpleBody = "Dear " + employee.getFullName() + ",\n\nYour leave request has been APPROVED.\n\nLeave Type: " + leaveRequest.getLeaveType() + "\nStart Date: " + leaveRequest.getStartDate() + "\nEnd Date: " + leaveRequest.getEndDate() + "\nDuration: " + leaveRequest.getLeaveDuration() + " days\n\nEnjoy your time off!";
            sendSimpleEmail(employee.getEmail(), subject, simpleBody);
        }
    }

    /**
     * Send leave rejection email
     */
    public void sendLeaveRejectedEmail(LeaveRequest leaveRequest, User employee, String rejectionReason) {
        String subject = "❌ Leave Request Rejected - " + leaveRequest.getLeaveType();
        
        try {
            String body = generateRejectionEmailBody(leaveRequest, employee, rejectionReason);
            sendHtmlEmail(employee.getEmail(), subject, body);
            System.out.println("📧 Email sent: Leave rejected for " + employee.getFullName());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending rejection email: " + e.getMessage());
            // Fallback to simple text email
            String simpleBody = "Dear " + employee.getFullName() + ",\n\nYour leave request has been REJECTED.\n\nLeave Type: " + leaveRequest.getLeaveType() + "\nStart Date: " + leaveRequest.getStartDate() + "\nEnd Date: " + leaveRequest.getEndDate() + "\nDuration: " + leaveRequest.getLeaveDuration() + " days\n\nRejection Reason: " + (rejectionReason != null ? rejectionReason : "Not specified") + "\n\nIf you have questions, please contact HR.";
            sendSimpleEmail(employee.getEmail(), subject, simpleBody);
        }
    }

    /**
     * Send leave reminder email
     */
    public void sendLeaveReminderEmail(LeaveRequest leaveRequest, User employee) {
        String subject = "📅 Leave Reminder - " + leaveRequest.getLeaveType();
        
        try {
            String body = generateReminderEmailBody(leaveRequest, employee);
            sendHtmlEmail(employee.getEmail(), subject, body);
            System.out.println("📧 Email sent: Leave reminder for " + employee.getFullName());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending reminder email: " + e.getMessage());
            // Fallback to simple text email
            String simpleBody = "Dear " + employee.getFullName() + ",\n\nThis is a reminder about your upcoming approved leave.\n\nLeave Type: " + leaveRequest.getLeaveType() + "\nStart Date: " + leaveRequest.getStartDate() + "\nEnd Date: " + leaveRequest.getEndDate() + "\nDuration: " + leaveRequest.getLeaveDuration() + " days\n\nPlease ensure you have completed all pending work and notified your team.\n\nHave a great time off!";
            sendSimpleEmail(employee.getEmail(), subject, simpleBody);
        }
    }

    /**
     * Generate approval email body (HTML)
     */
    private String generateApprovalEmailBody(LeaveRequest leaveRequest, User employee) {
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
                    
                    <p>Great news! Your leave request has been <strong>approved</strong> by HR.</p>
                    
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
                    
                    <p>If you have any questions, please contact HR.</p>
                    
                    <p>Enjoy your time off! 🎉</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e1e5e9; text-align: center; color: #6b7280; font-size: 12px;">
                        <p>This is an automated message from LeaveEase HR System</p>
                        <p>Employee Code: %s | Department: %s</p>
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
            employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "N/A",
            employee.getDepartment() != null ? employee.getDepartment() : "N/A"
        );
    }

    /**
     * Generate rejection email body (HTML)
     */
    private String generateRejectionEmailBody(LeaveRequest leaveRequest, User employee, String rejectionReason) {
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
                    
                    <p>We regret to inform you that your leave request has been <strong>rejected</strong> by HR.</p>
                    
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
                    
                    <p>If you believe this decision was made in error or if you have any questions, please contact HR for clarification.</p>
                    
                    <p>You may submit a new leave request with appropriate modifications.</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e1e5e9; text-align: center; color: #6b7280; font-size: 12px;">
                        <p>This is an automated message from LeaveEase HR System</p>
                        <p>Employee Code: %s | Department: %s</p>
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
            employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "N/A",
            employee.getDepartment() != null ? employee.getDepartment() : "N/A"
        );
    }

    /**
     * Generate reminder email body (HTML)
     */
    private String generateReminderEmailBody(LeaveRequest leaveRequest, User employee) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;">
                <div style="background: linear-gradient(135deg, #ffc107 0%, #e0a800 100%); color: white; padding: 20px; border-radius: 10px; text-align: center; margin-bottom: 20px;">
                    <h1 style="margin: 0; font-size: 24px;">📅 Leave Reminder</h1>
                    <p style="margin: 10px 0 0 0; opacity: 0.9;">LeaveEase HR System</p>
                </div>
                
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px;">
                    <h2 style="color: #ffc107; margin-top: 0;">Dear %s,</h2>
                    
                    <p>This is a friendly reminder about your upcoming approved leave.</p>
                    
                    <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ffc107;">
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
                        <li>Completed all pending work</li>
                        <li>Handed over responsibilities to your team</li>
                        <li>Set up your out-of-office message</li>
                        <li>Notified your immediate supervisor</li>
                    </ul>
                    
                    <p>Have a great time off! 🎉</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #e1e5e9; text-align: center; color: #6b7280; font-size: 12px;">
                        <p>This is an automated message from LeaveEase HR System</p>
                        <p>Employee Code: %s | Department: %s</p>
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
            employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "N/A",
            employee.getDepartment() != null ? employee.getDepartment() : "N/A"
        );
    }

    /**
     * Send HTML email
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) throws MessagingException {
        if (fromEmail.isEmpty() || emailPassword.isEmpty()) {
            System.out.println("📧 Email configuration not set up. Logging notification instead:");
            System.out.println("📧 To: " + toEmail);
            System.out.println("📧 Subject: " + subject);
            System.out.println("📧 Body: " + htmlBody.substring(0, Math.min(200, htmlBody.length())) + "...");
            System.out.println("📧 [Please configure email settings in application.properties]");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            
            mailSender.send(message);
            System.out.println("📧 HTML Email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Error sending HTML email: " + e.getMessage());
            // Fallback to simple email
            sendSimpleEmail(toEmail, subject, htmlBody.replaceAll("<[^>]*>", ""));
        }
    }

    /**
     * Send simple text email (fallback)
     */
    private void sendSimpleEmail(String toEmail, String subject, String body) {
        if (fromEmail.isEmpty() || emailPassword.isEmpty()) {
            System.out.println("📧 Email configuration not set up. Logging notification instead:");
            System.out.println("📧 To: " + toEmail);
            System.out.println("📧 Subject: " + subject);
            System.out.println("📧 Body: " + body.substring(0, Math.min(200, body.length())) + "...");
            System.out.println("📧 [Please configure email settings in application.properties]");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            System.out.println("📧 Simple Email sent successfully to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending simple email: " + e.getMessage());
        }
    }

    /**
     * Test email configuration
     */
    public boolean testEmailConfiguration() {
        try {
            if (fromEmail.isEmpty() || emailPassword.isEmpty()) {
                System.out.println("⚠️ Email configuration not set up - using notification logging");
                return true; // Return true since logging works
            }
            
            // Test with a simple message
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(fromEmail);
            testMessage.setTo(fromEmail); // Send to self for testing
            testMessage.setSubject("Test Email from LeaveEase");
            testMessage.setText("This is a test email to verify email configuration is working.");
            
            mailSender.send(testMessage);
            System.out.println("✅ Email configuration test successful - test email sent!");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Email configuration test failed: " + e.getMessage());
            return false;
        }
    }
} 