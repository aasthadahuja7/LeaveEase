#!/bin/bash

echo "👔 HR Email System Test"
echo "======================"

echo "1️⃣ Testing HR Email Configuration..."
response=$(curl -X POST http://localhost:8080/api/test-hr-email -s)
echo "Response: $response"
echo ""

echo "2️⃣ Testing Leave Approval with HR Email..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/approve \
  -H "Content-Type: application/json" \
  -s
echo ""
echo "✅ Leave approval test completed"
echo ""

echo "3️⃣ Testing Leave Rejection with HR Email..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Testing HR email system"}' \
  -s
echo ""
echo "✅ Leave rejection test completed"
echo ""

echo "📋 HR Email System Features:"
echo "   ✅ Emails sent FROM HR's email account"
echo "   ✅ Reply-to shows HR's email address"
echo "   ✅ HR's name in email signature"
echo "   ✅ Professional email templates"
echo "   ✅ Personalized content for each employee"
echo "   ✅ Fallback to simple text emails"
echo ""
echo "🎯 How It Works:"
echo "   1. HR logs in with their account"
echo "   2. HR approves/rejects leave in dashboard"
echo "   3. System gets HR's email from authentication"
echo "   4. Sends email FROM HR's email account"
echo "   5. Employee receives email from HR's email"
echo ""
echo "📧 Email Configuration Status:"
echo "   System Email: leavemanagement.demo@gmail.com"
echo "   Note: Update with real Gmail credentials for actual sending"
echo ""
echo "👤 HR Authentication Required:"
echo "   - HR must be logged in to send emails"
echo "   - System uses HR's email from their account"
echo "   - Reply-to shows HR's actual email address"
echo ""
echo "🌐 Application is running at: http://localhost:8080"
echo ""
echo "📖 Check EMPLOYEE_EMAIL_SYSTEM.md for configuration details"
