# 🚀 Real Email Setup Guide for LeaveEase

This guide will help you set up real email notifications using N8N workflow automation.

## 📧 Email Configuration Options

### Option 1: Gmail SMTP (Recommended for Testing)

1. **Create a Gmail App Password:**
   - Go to your Google Account settings
   - Navigate to Security → 2-Step Verification → App passwords
   - Generate a new app password for "Mail"
   - Copy the 16-character password

2. **Update application.properties:**
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-16-char-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

### Option 2: N8N Workflow Integration (Production)

1. **Install N8N:**
   ```bash
   npm install n8n -g
   n8n start
   ```

2. **Create N8N Workflow:**
   - Access N8N at http://localhost:5678
   - Create a new workflow
   - Add a Webhook trigger node
   - Add an Email node (Gmail, SMTP, or other providers)
   - Configure email templates with AI-generated content

3. **Update application.properties:**
   ```properties
   n8n.webhook.url=http://localhost:5678/webhook/leave-notification
   n8n.api.key=your-n8n-api-key
   ```

## 🤖 AI Agent Features

The system now includes AI-powered notifications that:

- **Smart Email Generation**: AI creates personalized email content
- **Multi-channel Delivery**: Email + WhatsApp (via N8N)
- **Fallback System**: Direct email if N8N is unavailable
- **Real-time Processing**: Instant notifications when HR approves/rejects

## 🔧 Current Configuration

Your current setup uses:
- **Email Service**: Real SMTP (no more demo mode)
- **AI Notifications**: N8N webhook integration
- **Fallback**: Direct email if N8N fails
- **Database**: Real MongoDB with employee data

## 📋 Testing Steps

1. **Test Email Configuration:**
   ```bash
   curl -X POST http://localhost:8080/api/test-email
   ```

2. **Test N8N Connection:**
   ```bash
   curl -X POST http://localhost:8080/api/test-n8n
   ```

3. **Test Leave Approval/Rejection:**
   - Log in to HR dashboard
   - Approve or reject a leave request
   - Check employee's email inbox

## 🎯 Features Working Now

✅ **Real Email Sending**: Employees receive actual emails  
✅ **AI-Powered Content**: Personalized email templates  
✅ **N8N Integration**: Workflow automation ready  
✅ **Fallback System**: Reliable delivery  
✅ **Database Integration**: Real employee data  

## 🚨 Important Notes

- **Gmail App Passwords**: Required for Gmail SMTP
- **N8N Setup**: Optional but recommended for production
- **Email Templates**: AI-generated with personalization
- **Security**: All emails sent from configured SMTP account

## 🔄 Next Steps

1. Configure your email settings in `application.properties`
2. Set up N8N workflow (optional)
3. Test the system with real employee emails
4. Monitor email delivery and N8N logs

Your LeaveEase system is now ready for production use with real email notifications! 🎉
