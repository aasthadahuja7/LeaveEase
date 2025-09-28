# 📧 Email Setup Instructions for LeaveEase

## 🚨 Current Issue
Employees are not receiving emails because the system is using demo credentials that don't work with Gmail SMTP.

## 🔧 Step-by-Step Fix

### Step 1: Create Gmail App Password

1. **Go to Google Account**: https://myaccount.google.com/
2. **Click on Security** (left sidebar)
3. **Enable 2-Step Verification** (if not already enabled)
4. **Go to App passwords** (under 2-Step Verification)
5. **Select "Mail"** from the dropdown
6. **Click "Generate"**
7. **Copy the 16-character password** (e.g., `abcd efgh ijkl mnop`)

### Step 2: Update application.properties

Open `src/main/resources/application.properties` and update these lines:

```properties
# Replace with your real Gmail address
spring.mail.username=your-email@gmail.com

# Replace with your 16-character app password
spring.mail.password=your-16-char-app-password
```

**Example:**
```properties
spring.mail.username=mycompany.hr@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

### Step 3: Restart Application

The application will automatically restart when you save the file.

### Step 4: Test Email System

Run this command to test:
```bash
curl -X POST http://localhost:8080/api/test-email -s
```

Expected response:
```json
{"status":"success","message":"Email configuration is working correctly"}
```

## 🎯 What Will Happen After Fix

✅ **HR approves leave** → Employee gets real email  
✅ **HR rejects leave** → Employee gets real email with reason  
✅ **AI-powered content** → Personalized email templates  
✅ **Database integration** → Uses real employee email addresses  

## 📋 Employee Email Requirements

Make sure employees in your database have **real email addresses**:

```javascript
// Example employee data
{
  "fullName": "John Doe",
  "email": "john.doe@company.com",  // Must be real email
  "username": "johndoe"
}
```

## 🚨 Common Issues

1. **"Invalid credentials"** → Check app password is correct
2. **"2-Step Verification required"** → Enable it first
3. **"Less secure app access"** → Use app passwords instead
4. **"Email not found"** → Check employee email in database

## 🎉 Success Indicators

- ✅ Email test returns `{"status":"success"}`
- ✅ HR can approve/reject leaves without errors
- ✅ Employees receive emails at their registered addresses
- ✅ Application logs show "Email sent successfully"

Your LeaveEase system will then send real emails to employees! 🚀
