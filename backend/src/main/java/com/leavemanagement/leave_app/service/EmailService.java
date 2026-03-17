package com.leavemanagement.leave_app.service;

import org.springframework.stereotype.Service;

/**
 * Simple email sending service stub. Replace with real implementation (JavaMailSender, etc.).
 */
@Service
public class EmailService {
    /**
     * Send an HTML email to the specified address.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        // Placeholder implementation - log to console for now.
        System.out.println("[EmailService] sendHtmlEmail to=" + to + " subject=" + subject);
        // In a real app you would use JavaMailSender or similar here.
    }
}
