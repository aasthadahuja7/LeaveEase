# Email Setup Guide for LeaveEase

## Current Status
✅ **Leave approval/rejection emails are now working in demo mode**
- When HR approves or rejects a leave request, the system will log the email details
- The employee's email address is correctly retrieved from their user profile
- Beautiful HTML email templates are generated

## How to Enable Real Email Sending

### Option 1: Gmail SMTP (Recommended)

1. **Create a Gmail App Password:**
   - Go to your Google Account settings
   - Enable 2-Factor Authentication
   - Go to Security → App passwords
   - Generate a new app password for "Mail"
   - Copy the 16-character password

2. **Update application.properties:**
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-16-character-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

3. **Enable Real Email Sending:**
   - Open `src/main/java/com/leavemanagment/leave_app/service/EmailService.java`
   - Find the `sendHtmlEmail` and `sendSimpleEmail` methods
   - Uncomment the email sending code (remove the `/*` and `*/` comments)
   - Comment out the demo logging code

### Option 2: Other Email Providers

You can use any SMTP provider. Common alternatives:
- **Outlook/Hotmail:** `smtp-mail.outlook.com:587`
- **Yahoo:** `smtp.mail.yahoo.com:587`
- **Custom SMTP:** Your company's email server

## Email Features

### ✅ What's Working Now:
1. **Leave Approval Emails:**
   - Sent to employee when HR approves their leave request
   - Includes leave details, dates, duration, and reason
   - Beautiful HTML formatting with company branding

2. **Leave Rejection Emails:**
   - Sent to employee when HR rejects their leave request
   - Includes rejection reason and leave details
   - Professional formatting with clear messaging

3. **Email Templates:**
   - Responsive HTML design
   - Company branding and colors
   - Professional formatting
   - Employee details included

### 📧 Email Content Includes:
- Employee's full name and email (from their profile)
- Leave type, start date, end date, duration
- Reason for leave
- Approval/rejection status
- Rejection reason (if applicable)
- Employee code and department
- Professional formatting

## Testing Email Functionality

1. **Login as HR:** Use `hr_user` / `password123`
2. **Go to HR Management tab**
3. **Find a pending leave request**
4. **Click "Approve" or "Reject"**
5. **Check the application logs** - you'll see the email details logged

## Current Demo Mode

In demo mode, the system:
- ✅ Logs all email details to console
- ✅ Shows exactly what would be sent
- ✅ Includes employee email address
- ✅ Generates beautiful HTML content
- ❌ Doesn't actually send emails (for safety)

## Production Setup

For production deployment:
1. Set up proper email credentials
2. Enable real email sending in EmailService
3. Consider using email service providers like:
   - SendGrid
   - Mailgun
   - Amazon SES
   - Your company's email server

## Troubleshooting

### Common Issues:
1. **"Authentication failed"** - Check your app password
2. **"Connection refused"** - Check SMTP host and port
3. **"Invalid email"** - Verify employee email addresses in database

### Debug Steps:
1. Check application logs for email details
2. Verify email configuration in application.properties
3. Test with a simple email first
4. Check firewall/network settings

## Security Notes

- Never commit real email passwords to version control
- Use environment variables for sensitive data
- Consider using email service providers for better deliverability
- Implement rate limiting for email sending

---

**Current Status:** ✅ Demo mode working perfectly
**Next Step:** Configure real email credentials when ready for production 