# 📧 Alternative Email Providers - LeaveEase

## 🚨 **If Gmail App Passwords Don't Work**

If you can't find Gmail App passwords, here are alternative email providers you can use:

## 🔧 **Option 1: Outlook/Hotmail**

### **Configuration:**
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-regular-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### **Setup:**
1. Use your regular Outlook password
2. No app password needed
3. Enable "Less secure app access" if prompted

## 🔧 **Option 2: Yahoo Mail**

### **Configuration:**
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=your-email@yahoo.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### **Setup:**
1. Go to Yahoo Account Security
2. Generate App Password
3. Use the app password

## 🔧 **Option 3: ProtonMail**

### **Configuration:**
```properties
spring.mail.host=127.0.0.1
spring.mail.port=1025
spring.mail.username=your-email@protonmail.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

## 🔧 **Option 4: Custom SMTP Server**

### **Configuration:**
```properties
spring.mail.host=your-smtp-server.com
spring.mail.port=587
spring.mail.username=your-email@yourdomain.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 🔧 **Option 5: Gmail Alternative Method**

### **Try This Gmail Setup:**
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=465
spring.mail.username=your-email@gmail.com
spring.mail.password=your-regular-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=false
spring.mail.properties.mail.smtp.socketFactory.port=465
spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
```

## 🧪 **Testing Alternative Providers**

### **Test Command:**
```bash
curl -X POST http://localhost:8080/api/test-email -s
```

### **Expected Response:**
```json
{"status":"success","message":"Email configuration is working correctly"}
```

## 🎯 **Recommended: Try Outlook First**

**Outlook is the easiest alternative:**
1. **No app password needed**
2. **Uses regular password**
3. **Usually works immediately**

### **Outlook Configuration:**
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-regular-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## 📋 **Steps to Use Alternative Provider**

1. **Choose an email provider** from the options above
2. **Update application.properties** with the new settings
3. **Save the file** (application will restart)
4. **Test the configuration** with the test command
5. **Verify emails are sent** when HR approves/rejects leaves

## 🚨 **Important Notes**

- **Outlook**: Usually works with regular password
- **Yahoo**: Requires app password (easier to find than Gmail)
- **Custom SMTP**: Use your company's email server
- **Test first**: Always test before using in production

## 🎉 **Success Indicators**

- ✅ Email test returns success
- ✅ HR can approve/reject leaves
- ✅ Employees receive emails
- ✅ No authentication errors

**Try Outlook first - it's usually the easiest alternative!** 🚀
