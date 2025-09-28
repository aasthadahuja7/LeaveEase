# 📧 Real Email Setup Guide

## 🚀 Quick Setup Options

### Option 1: Gmail (Recommended)
1. **Enable 2FA on your Gmail account**
2. **Generate App Password:**
   - Go to: https://myaccount.google.com/security
   - Click "2-Step Verification"
   - Scroll to "App passwords"
   - Generate password for "Mail"
   - Copy the 16-character code

3. **Update application.properties:**
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
```

### Option 2: Outlook/Hotmail
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Option 3: Yahoo Mail
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=your-email@yahoo.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 🧪 Test Email Configuration

After updating the configuration:

1. **Restart the application**
2. **Test with console command:**
```javascript
// Test real email sending
fetch('/leaves/test-email', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        testEmail: 'your-test-email@gmail.com'
    })
})
.then(response => response.json())
.then(data => console.log('Email test result:', data));
```

## 📧 What Employees Will Receive

Once configured, employees will get beautiful HTML emails with:
- ✅ Professional formatting
- 🎨 Company branding
- 📅 Complete leave details
- 🤖 AI-generated personalized content
- 📱 Mobile-responsive design
- 🔗 Action buttons (if needed)

## 🔧 Troubleshooting

### Common Issues:
1. **"Authentication failed"** - Check app password
2. **"Connection timeout"** - Check firewall/network
3. **"Invalid credentials"** - Verify email and password
4. **"Less secure apps"** - Use app password instead

### Debug Steps:
1. Check application logs for email errors
2. Verify SMTP settings
3. Test with a simple email client first
4. Check spam folder for test emails