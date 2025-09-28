#!/bin/bash

echo "📧 LeaveEase Email Configuration Helper"
echo "======================================"
echo ""

echo "🚨 Current Status:"
echo "   Your system is using DEMO email credentials"
echo "   These will NOT work with Gmail SMTP"
echo ""

echo "📋 Steps to Configure Real Email:"
echo ""
echo "1️⃣ Create Gmail App Password:"
echo "   - Go to: https://myaccount.google.com/"
echo "   - Security → 2-Step Verification → App passwords"
echo "   - Select 'Mail' and generate password"
echo "   - Copy the 16-character password"
echo ""

echo "2️⃣ Update application.properties:"
echo "   - Open: src/main/resources/application.properties"
echo "   - Change these lines:"
echo "     spring.mail.username=your-real-email@gmail.com"
echo "     spring.mail.password=your-16-char-app-password"
echo ""

echo "3️⃣ Test Current Configuration:"
echo "   Testing email configuration..."
response=$(curl -X POST http://localhost:8080/api/test-email -s 2>/dev/null)
echo "   Response: $response"
echo ""

if [[ $response == *"success"* ]]; then
    echo "✅ Email configuration is working!"
    echo "   Your employees will receive real emails."
else
    echo "❌ Email configuration needs to be updated."
    echo "   Please follow the steps above."
fi

echo ""
echo "🧪 Test Commands:"
echo "   curl -X POST http://localhost:8080/api/test-email -s"
echo "   curl -X POST http://localhost:8080/api/test-employee-email -s"
echo "   curl -X POST http://localhost:8080/api/test-hr-email -s"
echo ""

echo "📖 For detailed instructions, see:"
echo "   - EMAIL_CONFIGURATION_GUIDE.md"
echo "   - EMPLOYEE_EMAIL_SYSTEM.md"
echo "   - HR_EMAIL_SYSTEM.md"
echo ""

echo "🌐 Application is running at: http://localhost:8080"
