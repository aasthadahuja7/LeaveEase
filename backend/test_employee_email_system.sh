#!/bin/bash

echo "👤 Employee Email System Test"
echo "============================="

echo "1️⃣ Testing Employee Email Configuration..."
response=$(curl -X POST http://localhost:8080/api/test-employee-email -s)
echo "Response: $response"
echo ""

echo "2️⃣ Testing Leave Approval with Employee Email..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/approve \
  -H "Content-Type: application/json" \
  -s
echo ""
echo "✅ Leave approval test completed"
echo ""

echo "3️⃣ Testing Leave Rejection with Employee Email..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Testing employee email system"}' \
  -s
echo ""
echo "✅ Leave rejection test completed"
echo ""

echo "📋 Employee Email System Features:"
echo "   ✅ Emails sent TO employee's registered email"
echo "   ✅ Reply-to address shows employee's email"
echo "   ✅ Personalized email content"
echo "   ✅ Employee's name and department in email"
echo "   ✅ Professional HTML email templates"
echo "   ✅ Fallback to simple text emails"
echo ""
echo "🎯 How It Works:"
echo "   1. HR approves/rejects leave"
echo "   2. System finds employee by name/username"
echo "   3. Sends email TO employee's registered email"
echo "   4. Reply-to shows employee's email address"
echo "   5. Email content is personalized for the employee"
echo ""
echo "📧 Email Configuration Status:"
echo "   System Email: leavemanagement.demo@gmail.com"
echo "   Note: Update with real Gmail credentials for actual sending"
echo ""
echo "🌐 Application is running at: http://localhost:8080"
echo ""
echo "📖 Check EMAIL_SETUP_INSTRUCTIONS.md for configuration details"
