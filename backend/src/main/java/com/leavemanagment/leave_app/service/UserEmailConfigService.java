package com.leavemanagment.leave_app.service;

import com.leavemanagment.leave_app.model.User;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

/**
 * User Email Configuration Service
 * Manages email credentials for HR users to send emails from their personal accounts
 * Uses file-based persistence for configuration storage
 */
@Service
public class UserEmailConfigService {

    // File path for storing email configurations
    private static final String CONFIG_FILE_PATH = "email-configs.properties";
    
    // In-memory cache for quick access
    private Map<String, String> userEmailPasswords = new HashMap<>();
    
    // Default email configurations for different providers
    private Map<String, EmailConfig> emailConfigs = new HashMap<>();
    
    public UserEmailConfigService() {
        // Initialize email configurations for different providers
        emailConfigs.put("gmail.com", new EmailConfig("smtp.gmail.com", 587, true, true));
        emailConfigs.put("outlook.com", new EmailConfig("smtp-mail.outlook.com", 587, true, true));
        emailConfigs.put("hotmail.com", new EmailConfig("smtp-mail.outlook.com", 587, true, true));
        emailConfigs.put("yahoo.com", new EmailConfig("smtp.mail.yahoo.com", 587, true, true));
        
        // Load existing configurations from file
        loadConfigurationsFromFile();
    }
    
    /**
     * Load configurations from file on startup
     */
    private void loadConfigurationsFromFile() {
        try {
            Path configPath = Paths.get(CONFIG_FILE_PATH);
            if (Files.exists(configPath)) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
                    props.load(fis);
                    for (String key : props.stringPropertyNames()) {
                        userEmailPasswords.put(key, props.getProperty(key));
                    }
                    System.out.println("📧 Loaded " + userEmailPasswords.size() + " email configurations from file");
                }
            } else {
                System.out.println("📧 No existing email configuration file found, starting fresh");
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading email configurations from file: " + e.getMessage());
        }
    }
    
    /**
     * Save configurations to file for persistence
     */
    private void saveConfigurationsToFile() {
        try {
            Properties props = new Properties();
            for (Map.Entry<String, String> entry : userEmailPasswords.entrySet()) {
                props.setProperty(entry.getKey(), entry.getValue());
            }
            
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE_PATH)) {
                props.store(fos, "HR Email Configurations - Generated automatically");
                System.out.println("📧 Email configurations saved to file successfully");
            }
        } catch (Exception e) {
            System.err.println("❌ Error saving email configurations to file: " + e.getMessage());
        }
    }
    
    /**
     * Set email password for a user (should be app password for Gmail)
     */
    public void setUserEmailPassword(String userEmail, String appPassword) {
        try {
            userEmailPasswords.put(userEmail, appPassword);
            saveConfigurationsToFile(); // Persist to file immediately
            System.out.println("📧 Email configuration saved for user: " + userEmail);
        } catch (Exception e) {
            System.err.println("❌ Error saving email configuration for " + userEmail + ": " + e.getMessage());
        }
    }
    
    /**
     * Get email password for a user
     */
    public String getUserEmailPassword(String userEmail) {
        return userEmailPasswords.get(userEmail);
    }
    
    /**
     * Check if user has email configuration
     */
    public boolean hasEmailConfig(String userEmail) {
        String password = userEmailPasswords.get(userEmail);
        return password != null && !password.equals("PLACEHOLDER_APP_PASSWORD");
    }
    
    /**
     * Delete email configuration for a user
     */
    public void deleteUserEmailConfig(String userEmail) {
        try {
            userEmailPasswords.remove(userEmail);
            saveConfigurationsToFile(); // Persist changes to file
            System.out.println("📧 Email configuration deleted for user: " + userEmail);
        } catch (Exception e) {
            System.err.println("❌ Error deleting email configuration for " + userEmail + ": " + e.getMessage());
        }
    }
    
    /**
     * Get email configuration for a domain
     */
    public EmailConfig getEmailConfig(String email) {
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return emailConfigs.getOrDefault(domain, emailConfigs.get("gmail.com")); // Default to Gmail
    }
    
    /**
     * Initialize default HR email configurations for testing
     */
    public void initializeDefaultHRConfigs() {
        try {
            System.out.println("📧 Initializing default HR email configurations for testing...");
            
            // Only add placeholders if no configurations exist at all
            if (userEmailPasswords.isEmpty()) {
                userEmailPasswords.put("hr@company.com", "PLACEHOLDER_APP_PASSWORD");
                userEmailPasswords.put("admin@company.com", "PLACEHOLDER_APP_PASSWORD");
                saveConfigurationsToFile(); // Save to file
                System.out.println("📧 Default HR email configs initialized (placeholders only)");
            } else {
                System.out.println("📧 Email configurations already exist, skipping initialization");
            }
            
            System.out.println("📧 HR users should configure their real email credentials in production");
        } catch (Exception e) {
            System.err.println("❌ Error initializing default HR configs: " + e.getMessage());
        }
    }
    
    /**
     * Email configuration class
     */
    public static class EmailConfig {
        private String host;
        private int port;
        private boolean auth;
        private boolean starttls;
        
        public EmailConfig(String host, int port, boolean auth, boolean starttls) {
            this.host = host;
            this.port = port;
            this.auth = auth;
            this.starttls = starttls;
        }
        
        // Getters
        public String getHost() { return host; }
        public int getPort() { return port; }
        public boolean isAuth() { return auth; }
        public boolean isStarttls() { return starttls; }
    }
}