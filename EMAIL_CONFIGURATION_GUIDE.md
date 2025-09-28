# 📧 Email Configuration Guide - LeaveEase

## 🚨 **Current Status**
Your system is currently using **demo email credentials** that don't work with Gmail SMTP.

## 🔧 **Step-by-Step Configuration**

### **Step 1: Create Gmail App Password**

1. **Go to Google Account**: https://myaccount.google.com/
2. **Click "Security"** (left sidebar)
3. **Enable 2-Step Verification** (if not already enabled)
4. **Click "App passwords"** (under 2-Step Verification)
5. **Select "Mail"** from the dropdown
6. **Click "Generate"**
7. **Copy the 16-character password** (e.g., `abcd efgh ijkl mnop`)

### **Step 2: Update application.properties**

Open `src/main/resources/application.properties` and change these lines:

**❌ Current (Demo - Not Working):**
```properties
spring.mail.username=leavemanagement.demo@gmail.com
spring.mail.password=leavemanagement123
```

**✅ Replace with your real credentials:**
```properties
spring.mail.username=your-real-email@gmail.com
spring.mail.password=your-16-char-app-password
```

**Example:**
```properties
spring.mail.username=mycompany.hr@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```

### **Step 3: Restart Application**

The application will automatically restart when you save the file.

### **Step 4: Test Email Configuration**

Run this command to test:
```bash
curl -X POST http://localhost:8080/api/test-email -s
```

**Expected Success Response:**
```json
{"status":"success","message":"Email configuration is working correctly"}
```

## 🎯 **What Will Happen After Configuration**

### **✅ HR Approves Leave:**
- Employee receives email FROM HR's email account
- Subject: `✅ Leave Request Approved - [Leave Type]`
- HR's name in signature
- Reply-to shows HR's email

### **✅ HR Rejects Leave:**
- Employee receives email FROM HR's email account
- Subject: `❌ Leave Request Rejected - [Leave Type]`
- Rejection reason included
- HR's name in signature

## 📋 **Email Features You'll Get**

1. **Personal Communication**: Emails from actual HR person
2. **Professional Templates**: Beautiful HTML emails
3. **Employee Details**: Name, department, leave information
4. **HR Signature**: HR's name and contact information
5. **Reply Functionality**: Employees can reply directly to HR

## 🚨 **Common Issues & Solutions**

### **Issue 1: "Invalid credentials"**
- **Solution**: Check your app password is correct
- **Check**: Make sure you copied all 16 characters

### **Issue 2: "2-Step Verification required"**
- **Solution**: Enable 2-Step Verification first
- **Check**: Go to Security → 2-Step Verification

### **Issue 3: "Less secure app access"**
- **Solution**: Use App Passwords instead of regular password
- **Check**: Generate new app password for "Mail"

### **Issue 4: "Email not found"**
- **Solution**: Check employee email in database
- **Check**: Verify employee has real email address

## 🧪 **Testing Commands**

```bash
# Test basic email configuration
curl -X POST http://localhost:8080/api/test-email -s

# Test employee email system
curl -X POST http://localhost:8080/api/test-employee-email -s

# Test HR email system (requires HR login)
curl -X POST http://localhost:8080/api/test-hr-email -s

# Test complete notification system
curl -X POST http://localhost:8080/api/test-notification -s
```

## 📊 **Success Indicators**

- ✅ Email test returns `{"status":"success"}`
- ✅ HR can approve/reject leaves without errors
- ✅ Employees receive emails at their registered addresses
- ✅ Application logs show "Email sent successfully"
- ✅ Reply-to shows HR's actual email address

## 🔐 **Security Notes**

- **App Passwords**: More secure than regular passwords
- **2-Step Verification**: Required for app passwords
- **HR Authentication**: Required for sending emails
- **Audit Trail**: All emails are logged

## 🎉 **After Configuration**

Your LeaveEase system will:
1. **Send real emails** to employees
2. **Use HR's email account** for sending
3. **Include HR's name** in email signature
4. **Allow direct replies** to HR's email
5. **Provide professional communication**

## 📞 **Need Help?**

1. **Check logs**: Look at application console for error messages
2. **Test endpoints**: Use the test commands above
3. **Verify credentials**: Double-check your Gmail app password
4. **Check 2-Step Verification**: Must be enabled for app passwords

**Once configured, your employees will receive professional, personalized emails from HR when their leave requests are approved or rejected!** 🚀
