#!/bin/bash

echo "🧪 Testing HR Management Functionality"
echo "======================================"

BASE_URL="http://localhost:8080"

# Test 1: Check if HR endpoints are accessible
echo "1. Testing HR endpoints accessibility..."

# Test HR stats endpoint
echo "   - Testing HR stats endpoint..."
curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/dashboard/hr/employee-stats"
echo " - HR stats endpoint"

# Test pending requests endpoint
echo "   - Testing pending requests endpoint..."
curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/dashboard/hr/pending-requests"
echo " - Pending requests endpoint"

# Test all requests endpoint
echo "   - Testing all requests endpoint..."
curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/dashboard/hr/all-requests"
echo " - All requests endpoint"

echo ""
echo "2. Testing leave approval/rejection endpoints..."

# Test approval endpoint
echo "   - Testing approval endpoint..."
curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/leaves/test-id/approve"
echo " - Approval endpoint"

# Test rejection endpoint
echo "   - Testing rejection endpoint..."
curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/leaves/test-id/reject" \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Test rejection reason"}'
echo " - Rejection endpoint"

echo ""
echo "3. Testing HR action endpoint..."

# Test HR action endpoint
echo "   - Testing HR action endpoint..."
curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/leaves/test-id/hr-action" \
  -H "Content-Type: application/json" \
  -d '{"action": "approve", "reason": ""}'
echo " - HR action endpoint"

echo ""
echo "✅ HR functionality test completed!"
echo ""
echo "📋 Summary:"
echo "   - HR endpoints should return 200 (OK) or 404 (Not Found) for test IDs"
echo "   - All endpoints should be accessible"
echo "   - Rejection reason functionality should be working"
echo ""
echo "🌐 To test the full HR dashboard:"
echo "   1. Start the application: ./mvnw spring-boot:run"
echo "   2. Open: http://localhost:8080/dashboard.html?role=hr"
echo "   3. Create some leave requests as employees"
echo "   4. Test approval/rejection with reasons in the HR dashboard" 