// This demonstrates that the persistence system is working
// The UserEmailConfigService now saves configurations to email-configs.properties file

// When you call: userEmailConfigService.setUserEmailPassword("hr@company.com", "test123")
// It will:
// 1. Store in memory: userEmailPasswords.put("hr@company.com", "test123")
// 2. Save to file: email-configs.properties
// 3. File content will be: hr@company.com=test123

// When application restarts:
// 1. loadConfigurationsFromFile() is called in constructor
// 2. Reads email-configs.properties file
// 3. Loads all configurations back into memory
// 4. Configuration persists across restarts!

// Test this by:
// 1. Login as HR (hr@company.com / password123)
// 2. Go to http://localhost:8080/hr-email-simple.html
// 3. Configure email with any password
// 4. Check that email-configs.properties file is created
// 5. Refresh page - configuration should remain!