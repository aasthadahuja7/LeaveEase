#!/bin/bash

echo "🔍 Email System Debug Test"
echo "=========================="

echo "1️⃣ Testing Email Configuration..."
response=$(curl -X POST http://localhost:8080/api/test-email -s)
echo "Response: $response"
echo ""

echo "2️⃣ Testing Leave Approval (this should trigger email)..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/approve \
  -H "Content-Type: application/json" \
  -s
echo ""
echo "✅ Leave approval test completed"
echo ""

echo "3️⃣ Testing Leave Rejection (this should trigger email)..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Testing email system"}' \
  -s
echo ""
echo "✅ Leave rejection test completed"
echo ""

echo "📋 Current Email Configuration:"
echo "   Host: smtp.gmail.com"
echo "   Port: 587"
echo "   Username: leavemanagement.demo@gmail.com"
echo "   Password: leavemanagement123 (demo password)"
echo ""
echo "❌ Issue: Demo credentials won't work with Gmail SMTP"
echo ""
echo "🔧 To Fix This:"
echo "1. Create a Gmail App Password:"
echo "   - Go to https://myaccount.google.com/"
echo "   - Security → 2-Step Verification → App passwords"
echo "   - Generate app password for 'Mail'"
echo ""
echo "2. Update application.properties:"
echo "   spring.mail.username=your-real-email@gmail.com"
echo "   spring.mail.password=your-16-char-app-password"
echo ""
echo "3. Restart the application"
echo ""
echo "🌐 Application is running at: http://localhost:8080"
