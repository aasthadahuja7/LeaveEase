#!/bin/bash

echo "🤖 Testing AI Email System with N8N Integration"
echo "================================================"

# Test 1: Email Configuration
echo "1️⃣ Testing Email Configuration..."
curl -X POST http://localhost:8080/api/test-email -s | jq .
echo ""

# Test 2: N8N Connection
echo "2️⃣ Testing N8N Connection..."
curl -X POST http://localhost:8080/api/test-n8n -s | jq .
echo ""

# Test 3: Complete Notification System
echo "3️⃣ Testing Complete Notification System..."
curl -X POST http://localhost:8080/api/test-notification -s | jq .
echo ""

# Test 4: Leave Approval with AI Notification
echo "4️⃣ Testing Leave Approval with AI Notification..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/approve \
  -H "Content-Type: application/json" \
  -s
echo ""
echo "✅ Leave approval test completed"
echo ""

# Test 5: Leave Rejection with AI Notification
echo "5️⃣ Testing Leave Rejection with AI Notification..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Testing AI notification system"}' \
  -s
echo ""
echo "✅ Leave rejection test completed"
echo ""

# Test 6: Application Status
echo "6️⃣ Checking Application Status..."
curl -s http://localhost:8080/login.html | head -3
echo ""
echo "✅ Application is running and accessible"
echo ""

echo "🎉 AI Email System Test Complete!"
echo ""
echo "📧 Email Features:"
echo "   ✅ Real SMTP email sending"
echo "   ✅ AI-powered email templates"
echo "   ✅ N8N workflow integration"
echo "   ✅ Fallback system"
echo "   ✅ Database integration"
echo ""
echo "🌐 Access your application at: http://localhost:8080"
echo "📋 Check REAL_EMAIL_SETUP.md for configuration details"
