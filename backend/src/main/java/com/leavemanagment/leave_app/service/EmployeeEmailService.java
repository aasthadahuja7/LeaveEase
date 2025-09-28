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
public class EmployeeEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String systemEmail;

    @Value("${spring.mail.password:}")
    private String systemPassword;

    /**
     * Send leave approval email from employee's perspective
     */
    public void sendLeaveApprovedEmailFromEmployee(LeaveRequest leaveRequest, User employee) {
        try {
            String subject = "✅ Leave Request Approved - " + leaveRequest.getLeaveType();
            String htmlBody = generateEmployeeApprovalEmailBody(leaveRequest, employee);
            
            sendHtmlEmailFromEmployee(employee.getEmail(), subject, htmlBody, employee);
            System.out.println("📧 Employee approval email sent to: " + employee.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending employee approval email: " + e.getMessage());
        }
    }

    /**
     * Send leave rejection email from employee's perspective
     */
    public void sendLeaveRejectedEmailFromEmployee(LeaveRequest leaveRequest, User employee, String rejectionReason) {
        try {
            String subject = "❌ Leave Request Rejected - " + leaveRequest.getLeaveType();
            String htmlBody = generateEmployeeRejectionEmailBody(leaveRequest, employee, rejectionReason);
            
            sendHtmlEmailFromEmployee(employee.getEmail(), subject, htmlBody, employee);
            System.out.println("📧 Employee rejection email sent to: " + employee.getEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error sending employee rejection email: " + e.getMessage());
        }
    }

    /**
     * Generate approval email body from employee's perspective
     */
    private String generateEmployeeApprovalEmailBody(LeaveRequest leaveRequest, User employee) {
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
                        <p>Employee: %s | Department: %s</p>
                        <p>Email: %s</p>
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
            employee.getFullName(),
            employee.getDepartment() != null ? employee.getDepartment() : "N/A",
            employee.getEmail()
        );
    }

    /**
     * Generate rejection email body from employee's perspective
     */
    private String generateEmployeeRejectionEmailBody(LeaveRequest leaveRequest, User employee, String rejectionReason) {
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
                        <p>Employee: %s | Department: %s</p>
                        <p>Email: %s</p>
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
            employee.getFullName(),
            employee.getDepartment() != null ? employee.getDepartment() : "N/A",
            employee.getEmail()
        );
    }

    /**
     * Send HTML email from employee's perspective
     */
    private void sendHtmlEmailFromEmployee(String toEmail, String subject, String htmlBody, User employee) throws MessagingException {
        if (systemEmail.isEmpty() || systemPassword.isEmpty()) {
            System.out.println("📧 Email configuration not set up. Logging notification instead:");
            System.out.println("📧 From: " + employee.getEmail() + " (Employee's email)");
            System.out.println("📧 To: " + toEmail);
            System.out.println("📧 Subject: " + subject);
            System.out.println("📧 Body: " + htmlBody.substring(0, Math.min(200, htmlBody.length())) + "...");
            System.out.println("📧 [Please configure email settings in application.properties]");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set the "from" address to show the employee's email
            helper.setFrom(systemEmail); // System email for SMTP authentication
            helper.setReplyTo(employee.getEmail()); // Reply-to shows employee's email
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            
            mailSender.send(message);
            System.out.println("📧 Employee email sent successfully from: " + employee.getEmail() + " to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Error sending employee email: " + e.getMessage());
            // Fallback to simple email
            sendSimpleEmailFromEmployee(toEmail, subject, htmlBody.replaceAll("<[^>]*>", ""), employee);
        }
    }

    /**
     * Send simple text email from employee's perspective (fallback)
     */
    private void sendSimpleEmailFromEmployee(String toEmail, String subject, String body, User employee) {
        if (systemEmail.isEmpty() || systemPassword.isEmpty()) {
            System.out.println("📧 Email configuration not set up. Logging notification instead:");
            System.out.println("📧 From: " + employee.getEmail() + " (Employee's email)");
            System.out.println("📧 To: " + toEmail);
            System.out.println("📧 Subject: " + subject);
            System.out.println("📧 Body: " + body.substring(0, Math.min(200, body.length())) + "...");
            System.out.println("📧 [Please configure email settings in application.properties]");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(systemEmail); // System email for SMTP authentication
            message.setReplyTo(employee.getEmail()); // Reply-to shows employee's email
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            System.out.println("📧 Simple employee email sent successfully from: " + employee.getEmail() + " to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending simple employee email: " + e.getMessage());
        }
    }

    /**
     * Test employee email configuration
     */
    public boolean testEmployeeEmailConfiguration() {
        try {
            if (systemEmail.isEmpty() || systemPassword.isEmpty()) {
                System.out.println("📧 Employee email configuration not set up");
                return false;
            }

            // Test with a dummy employee
            User testEmployee = new User();
            testEmployee.setEmail("test@example.com");
            testEmployee.setFullName("Test Employee");
            testEmployee.setDepartment("Test Department");

            String testSubject = "Test Employee Email";
            String testBody = "This is a test email from employee perspective.";

            sendSimpleEmailFromEmployee("test@example.com", testSubject, testBody, testEmployee);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Employee email test failed: " + e.getMessage());
            return false;
        }
    }
}
