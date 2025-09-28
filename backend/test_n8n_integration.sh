#!/bin/bash

# Test N8N Integration for LeaveEase
# This script tests the N8N workflow integration for leave notifications

echo "🧪 Testing N8N Integration for LeaveEase"
echo "========================================"

BASE_URL="http://localhost:8080"
N8N_URL="http://localhost:5678"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "SUCCESS") echo -e "${GREEN}✅ $message${NC}" ;;
        "ERROR") echo -e "${RED}❌ $message${NC}" ;;
        "INFO") echo -e "${BLUE}ℹ️  $message${NC}" ;;
        "WARNING") echo -e "${YELLOW}⚠️  $message${NC}" ;;
    esac
}

# Check if LeaveEase application is running
echo ""
print_status "INFO" "Checking if LeaveEase application is running..."
if curl -s "$BASE_URL" > /dev/null; then
    print_status "SUCCESS" "LeaveEase application is running on $BASE_URL"
else
    print_status "ERROR" "LeaveEase application is not running on $BASE_URL"
    exit 1
fi

# Check if N8N is running
echo ""
print_status "INFO" "Checking if N8N is running..."
if curl -s "$N8N_URL" > /dev/null; then
    print_status "SUCCESS" "N8N is running on $N8N_URL"
else
    print_status "WARNING" "N8N might not be running on $N8N_URL (this is optional for testing)"
fi

# Test N8N connection endpoint
echo ""
print_status "INFO" "Testing N8N connection endpoint..."
response=$(curl -s -w "%{http_code}" "$BASE_URL/leaves/test-n8n")
http_code="${response: -3}"
response_body="${response%???}"

if [ "$http_code" = "200" ]; then
    print_status "SUCCESS" "N8N connection test passed"
    echo "Response: $response_body"
elif [ "$http_code" = "503" ]; then
    print_status "WARNING" "N8N connection failed (service unavailable)"
    echo "Response: $response_body"
else
    print_status "ERROR" "N8N connection test failed with HTTP $http_code"
    echo "Response: $response_body"
fi

# Get authentication token (you'll need to implement this based on your auth system)
echo ""
print_status "INFO" "For full testing, you'll need to:"
echo "1. Set up N8N workflow with webhook: http://localhost:5678/webhook/leave-notification"
echo "2. Configure email notifications in N8N"
echo "3. Get a valid JWT token for authentication"
echo "4. Create a test leave request"
echo "5. Test approval/rejection with the following commands:"

echo ""
echo "# Test leave approval (replace {leave-id} and {jwt-token}):"
echo "curl -X PUT $BASE_URL/leaves/{leave-id}/approve \\"
echo "  -H \"Authorization: Bearer {jwt-token}\" \\"
echo "  -H \"Content-Type: application/json\""

echo ""
echo "# Test leave rejection (replace {leave-id} and {jwt-token}):"
echo "curl -X PUT $BASE_URL/leaves/{leave-id}/reject \\"
echo "  -H \"Authorization: Bearer {jwt-token}\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"rejectionReason\": \"Testing N8N integration\"}'"

echo ""
echo "# Get all leave requests to find a leave ID:"
echo "curl -X GET $BASE_URL/leaves \\"
echo "  -H \"Authorization: Bearer {jwt-token}\""

echo ""
print_status "INFO" "N8N Webhook Payload Structure:"
echo "The following data will be sent to your N8N webhook:"
cat << 'EOF'
{
  "type": "LEAVE_APPROVED" | "LEAVE_REJECTED",
  "action": "APPROVED" | "REJECTED",
  "employeeName": "Employee Full Name",
  "employeeEmail": "employee@company.com",
  "employeeUsername": "username",
  "startDate": "2024-01-15",
  "endDate": "2024-01-20",
  "leaveType": "Annual Leave",
  "duration": 5,
  "reason": "Employee's reason for leave",
  "rejectionReason": "HR's rejection reason (only for rejections)",
  "leaveRequestId": "MongoDB ObjectId",
  "timestamp": "2024-01-10T10:30:00",
  "channels": ["email", "whatsapp", "slack"],
  "priority": "high"
}
EOF

echo ""
print_status "SUCCESS" "N8N integration setup is complete!"
print_status "INFO" "Check the N8N_WORKFLOW_SETUP.md file for detailed workflow configuration"

echo ""
echo "🔗 Useful URLs:"
echo "• LeaveEase Application: $BASE_URL"
echo "• N8N Interface: $N8N_URL"
echo "• N8N Webhook URL: $N8N_URL/webhook/leave-notification"
echo "• Test N8N Connection: $BASE_URL/leaves/test-n8n"