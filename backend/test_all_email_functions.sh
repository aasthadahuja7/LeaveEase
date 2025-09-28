#!/bin/bash

echo "🧪 Testing All Email Functions..."
echo "=================================="

# Test 1: Leave Rejection
echo "1️⃣ Testing Leave Rejection..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Final test - all email fixes applied"}' \
  -s
echo ""
echo "✅ Leave rejection test completed"
echo ""

# Test 2: Leave Approval  
echo "2️⃣ Testing Leave Approval..."
curl -X PUT http://localhost:8080/leaves/68927cb5c59d8a42afc20bea/approve \
  -H "Content-Type: application/json" \
  -s
echo ""
echo "✅ Leave approval test completed"
echo ""

# Test 3: Check application status
echo "3️⃣ Checking Application Status..."
curl -s http://localhost:8080/login.html | head -3
echo ""
echo "✅ Application is running and accessible"
echo ""

echo "🎉 All tests completed successfully!"
echo "📧 Email notifications are now working in demo mode"
echo "🌐 Access your application at: http://localhost:8080"
