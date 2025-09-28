# 👤 Employee Email System - LeaveEase

## 🎯 **What's New**

Your LeaveEase system now sends emails **from the employee's perspective**! When HR approves or rejects a leave request, the employee receives an email that appears to come from their own email account.

## 🚀 **How It Works**

### **Before (Old System):**
- HR email → Employee email
- Generic email templates
- Single sender address

### **After (New System):**
- Employee's email → Employee's email (personalized)
- Professional HTML templates
- Reply-to shows employee's email
- Personalized content with employee details

## 📧 **Email Features**

### **✅ What Employees Will See:**

1. **Email Address**: `employee@company.com` (their registered email)
2. **Reply-To**: `employee@company.com` (their own email)
3. **Subject**: `✅ Leave Request Approved - [Leave Type]` or `❌ Leave Request Rejected - [Leave Type]`
4. **Content**: Personalized with their name, department, and leave details

### **📋 Email Content Includes:**

**For Approval:**
- ✅ Green success header
- Employee's full name
- Leave type, dates, duration
- Approval confirmation
- Next steps checklist
- Contact information

**For Rejection:**
- ❌ Red rejection header
- Employee's full name
- Leave details
- Rejection reason
- Appeal process
- Contact information

## 🔧 **Technical Implementation**

### **System Architecture:**
```
HR Action → EmployeeEmailService → SMTP → Employee's Email
```

### **Key Components:**
1. **EmployeeEmailService**: New service for employee-specific emails
2. **Personalized Templates**: HTML emails with employee details
3. **Reply-To Headers**: Shows employee's email for replies
4. **Fallback System**: Simple text emails if HTML fails

### **Email Flow:**
1. HR approves/rejects leave in dashboard
2. System finds employee by name or username
3. Generates personalized email content
4. Sends email TO employee's registered email
5. Reply-to shows employee's email address

## 🎨 **Email Templates**

### **Approval Email:**
- Professional green gradient header
- Employee's name prominently displayed
- Leave details in organized format
- Action items checklist
- Company branding

### **Rejection Email:**
- Professional red gradient header
- Clear rejection notification
- Detailed rejection reason
- Appeal process information
- Support contact details

## 📊 **Database Integration**

### **Required Employee Data:**
```json
{
  "fullName": "John Doe",
  "email": "john.doe@company.com",
  "department": "Engineering",
  "username": "johndoe"
}
```

### **Email Lookup Process:**
1. Try to find employee by `fullName`
2. Fallback to `username` if not found
3. Use employee's `email` for sending
4. Include `department` in email content

## 🧪 **Testing the System**

### **Test Commands:**
```bash
# Test employee email configuration
curl -X POST http://localhost:8080/api/test-employee-email -s

# Test leave approval
curl -X PUT http://localhost:8080/leaves/[ID]/approve -H "Content-Type: application/json"

# Test leave rejection
curl -X PUT http://localhost:8080/leaves/[ID]/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Testing"}'
```

### **Test Script:**
```bash
./test_employee_email_system.sh
```

## ⚙️ **Configuration**

### **Email Settings (application.properties):**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### **Security Settings:**
- Test endpoints are publicly accessible
- Leave management requires authentication
- Employee data is protected

## 🎉 **Benefits**

### **For Employees:**
- ✅ Personalized email experience
- ✅ Professional email templates
- ✅ Clear approval/rejection details
- ✅ Easy reply functionality
- ✅ Mobile-friendly HTML design

### **For HR:**
- ✅ Automated email sending
- ✅ Professional communication
- ✅ Detailed audit trail
- ✅ Reduced manual work
- ✅ Consistent messaging

### **For Organization:**
- ✅ Improved employee experience
- ✅ Professional communication
- ✅ Automated workflow
- ✅ Better record keeping
- ✅ Enhanced transparency

## 🔍 **Troubleshooting**

### **Common Issues:**

1. **"Employee not found"**
   - Check employee name in database
   - Verify username fallback works
   - Review available users in logs

2. **"Email configuration failed"**
   - Update Gmail credentials
   - Enable 2-Step Verification
   - Generate App Password

3. **"Email not sent"**
   - Check SMTP settings
   - Verify email addresses
   - Review application logs

### **Debug Commands:**
```bash
# Check application logs
tail -f logs/application.log

# Test email configuration
curl -X POST http://localhost:8080/api/test-employee-email -s

# Check employee data
curl -X GET http://localhost:8080/api/users -H "Authorization: Bearer [token]"
```

## 🚀 **Next Steps**

1. **Configure Real Email**: Update `application.properties` with real Gmail credentials
2. **Test System**: Run the test script to verify functionality
3. **Monitor Logs**: Check application logs for email delivery status
4. **Employee Feedback**: Gather feedback on email experience
5. **Customize Templates**: Modify email templates as needed

## 📞 **Support**

- Check application logs for detailed error messages
- Review `EMAIL_SETUP_INSTRUCTIONS.md` for configuration help
- Test with `./test_employee_email_system.sh` script
- Monitor email delivery in application console

Your LeaveEase system now provides a professional, personalized email experience for all employees! 🎉
