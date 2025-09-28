#!/bin/bash

echo "🧪 Testing Leave Rejection Functionality"
echo "========================================"

# Test 1: Try to reject a leave request for "John Doe" (exists in users)
echo "📋 Test 1: Rejecting leave for 'John Doe' (should work)"
curl -X PUT http://localhost:8080/leaves/688696f34103b4f3bd27ded3/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Test rejection for existing user"}' \
  -s | jq '.' 2>/dev/null || echo "Response received"

echo ""
echo "📋 Test 2: Rejecting leave for 'Michael Chen' (should show fallback info)"
curl -X PUT http://localhost:8080/leaves/688697104103b4f3bd27ded5/reject \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Test rejection for non-existing user"}' \
  -s | jq '.' 2>/dev/null || echo "Response received"

echo ""
echo "✅ Test completed! Check the application logs for email sending details." 