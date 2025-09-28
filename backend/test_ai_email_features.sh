#!/bin/bash

# Test AI Email Features for LeaveEase
# This script tests the AI-powered email generation features

echo "🤖 Testing AI Email Features for LeaveEase"
echo "=========================================="

BASE_URL="http://localhost:8080"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
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
        "AI") echo -e "${PURPLE}🤖 $message${NC}" ;;
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

echo ""
print_status "AI" "AI Email Features Available:"
echo "1. 🧠 OpenAI Integration - Advanced AI-generated emails"
echo "2. 🏠 Local AI (Ollama) - Privacy-focused local AI"
echo "3. 🎨 Smart Templates - Intelligent template engine"
echo "4. 📧 Fallback System - Always works, even without AI"

echo ""
print_status "INFO" "New AI-Powered Endpoints:"
echo "• POST $BASE_URL/leaves/{id}/ai-approve - AI-powered approval emails"
echo "• POST $BASE_URL/leaves/{id}/ai-reject - AI-powered rejection emails"

echo ""
print_status "INFO" "Testing requires authentication. Here's how to test:"

echo ""
echo "📋 Step 1: Get Authentication Token"
echo "1. Open browser: $BASE_URL"
echo "2. Login with HR credentials"
echo "3. Open Developer Tools (F12)"
echo "4. Go to Application/Storage → Local Storage"
echo "5. Copy the JWT token value"

echo ""
echo "📋 Step 2: Get Leave Request ID"
echo "curl -X GET $BASE_URL/leaves \\"
echo "  -H \"Authorization: Bearer {your-jwt-token}\""

echo ""
echo "📋 Step 3: Test AI Approval"
echo "curl -X PUT $BASE_URL/leaves/{leave-id}/ai-approve \\"
echo "  -H \"Authorization: Bearer {your-jwt-token}\" \\"
echo "  -H \"Content-Type: application/json\""

echo ""
echo "📋 Step 4: Test AI Rejection"
echo "curl -X PUT $BASE_URL/leaves/{leave-id}/ai-reject \\"
echo "  -H \"Authorization: Bearer {your-jwt-token}\" \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"rejectionReason\": \"Testing AI-powered rejection email\"}'"

echo ""
print_status "AI" "AI Email Generation Process:"
echo "1. 🧠 Try OpenAI (if API key configured)"
echo "2. 🎨 Fallback to Smart Templates (always available)"
echo "3. 📧 Final fallback to Basic Templates"

echo ""
print_status "INFO" "AI Features Configuration:"
echo "Edit application.properties to configure:"
cat << 'EOF'

# OpenAI Configuration (Optional)
openai.api.key=your-openai-api-key-here
openai.api.url=https://api.openai.com/v1/chat/completions

# Local AI Configuration (Optional)
ollama.api.url=http://localhost:11434/api/generate
ollama.model=llama2

# AI Features
ai.email.enabled=true
ai.smart.templates.enabled=true
EOF

echo ""
print_status "AI" "Smart Template Features:"
echo "• 🎯 Personalized greetings based on employee name"
echo "• 📅 Leave type-specific messages (vacation, sick, emergency)"
echo "• 🌟 Duration-aware content (short vs long leaves)"
echo "• 🌸 Seasonal messages based on leave dates"
echo "• 📋 Smart preparation checklists"
echo "• 💡 Alternative suggestions for rejections"
echo "• 🎨 Beautiful HTML email templates"
echo "• 📱 Mobile-responsive design"

echo ""
print_status "AI" "Email Content Examples:"

echo ""
echo "📧 Approval Email Features:"
echo "• Personalized greeting with employee's first name"
echo "• Leave type-specific congratulatory messages"
echo "• Duration-aware preparation tips"
echo "• Seasonal well-wishes"
echo "• Professional HTML formatting with colors and emojis"
echo "• Comprehensive leave details table"
echo "• Smart preparation checklist based on leave duration"

echo ""
echo "📧 Rejection Email Features:"
echo "• Empathetic and understanding tone"
echo "• Clear explanation of rejection reason"
echo "• Alternative suggestions based on rejection type"
echo "• Encouragement to discuss or reapply"
echo "• Professional formatting with helpful next steps"
echo "• Contact information for HR discussion"

echo ""
print_status "SUCCESS" "AI Email Features Setup Complete!"

echo ""
print_status "INFO" "Sample AI-Generated Email Response:"
cat << 'EOF'
{
  "success": true,
  "message": "Leave approved and AI-powered email sent successfully",
  "aiMethod": "Smart Template",
  "employeeEmail": "employee@company.com",
  "leaveRequest": {
    "id": "507f1f77bcf86cd799439011",
    "status": "Approved",
    "leaveType": "Annual Leave",
    "startDate": "2024-01-15",
    "endDate": "2024-01-20"
  }
}
EOF

echo ""
print_status "AI" "Advanced Features Available:"
echo "1. 🔄 Multi-level fallback system"
echo "2. 🎨 Dynamic content based on leave context"
echo "3. 📊 AI method reporting in API responses"
echo "4. 🛡️ Error handling with graceful degradation"
echo "5. 📱 Mobile-responsive email templates"
echo "6. 🌍 Seasonal and contextual messaging"
echo "7. 💼 Professional HR communication standards"

echo ""
print_status "INFO" "Optional Integrations:"
echo "• OpenAI API - For advanced natural language generation"
echo "• Ollama - For local AI processing (privacy-focused)"
echo "• Custom AI models - Easily extensible architecture"

echo ""
print_status "SUCCESS" "Ready to test AI-powered email notifications!"
print_status "INFO" "The system will automatically choose the best available AI method"

echo ""
echo "🔗 Useful URLs:"
echo "• LeaveEase Application: $BASE_URL"
echo "• AI Approval Endpoint: $BASE_URL/leaves/{id}/ai-approve"
echo "• AI Rejection Endpoint: $BASE_URL/leaves/{id}/ai-reject"
echo "• Regular Leaves API: $BASE_URL/leaves"