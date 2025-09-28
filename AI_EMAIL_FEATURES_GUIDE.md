# 🤖 AI Email Features Guide for LeaveEase

## Overview
LeaveEase now includes powerful AI-driven email generation features that create personalized, professional, and contextually appropriate email notifications for leave approvals and rejections.

## 🎯 Key Features

### ✨ **Smart Email Generation**
- **Personalized Content**: Emails are tailored to each employee and situation
- **Context-Aware**: Different messages for different leave types and durations
- **Professional Tone**: Maintains HR communication standards
- **Multi-Language Ready**: Extensible for international organizations

### 🔄 **Multi-Tier AI System**
1. **OpenAI Integration** (Premium) - Advanced natural language generation
2. **Local AI (Ollama)** (Privacy-focused) - On-premises AI processing
3. **Smart Templates** (Always Available) - Intelligent template engine
4. **Basic Fallback** (Guaranteed) - Simple but effective templates

## 🚀 **Available Options**

### Option 1: OpenAI Integration (Recommended for Advanced AI)

**Features:**
- ✅ Most advanced natural language generation
- ✅ Highly personalized content
- ✅ Context-aware messaging
- ✅ Professional tone adaptation
- ❌ Requires API key and internet connection
- ❌ Usage costs apply

**Setup:**
```properties
# application.properties
openai.api.key=your-openai-api-key-here
openai.api.url=https://api.openai.com/v1/chat/completions
```

**How to get OpenAI API Key:**
1. Go to [OpenAI Platform](https://platform.openai.com/)
2. Create account or sign in
3. Navigate to API Keys section
4. Create new API key
5. Add to application.properties

### Option 2: Local AI with Ollama (Privacy-Focused)

**Features:**
- ✅ Complete privacy - no data leaves your server
- ✅ No usage costs after setup
- ✅ Customizable AI models
- ✅ Good quality email generation
- ❌ Requires local AI setup
- ❌ Higher resource usage

**Setup:**
```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Download a model (e.g., Llama 2)
ollama pull llama2

# Start Ollama service
ollama serve
```

```properties
# application.properties
ollama.api.url=http://localhost:11434/api/generate
ollama.model=llama2
```

### Option 3: Smart Templates (Always Available)

**Features:**
- ✅ No external dependencies
- ✅ Always works
- ✅ Intelligent content selection
- ✅ Professional templates
- ✅ Fast performance
- ✅ Highly customizable

**Automatic Features:**
- Personalized greetings based on employee names
- Leave type-specific messages
- Duration-aware content
- Seasonal messaging
- Smart preparation checklists
- Alternative suggestions for rejections

## 📧 **New API Endpoints**

### AI-Powered Approval
```http
PUT /leaves/{id}/ai-approve
Authorization: Bearer {jwt-token}
Content-Type: application/json
```

**Response:**
```json
{
  "success": true,
  "message": "Leave approved and AI-powered email sent successfully",
  "aiMethod": "Smart Template",
  "employeeEmail": "employee@company.com",
  "leaveRequest": {
    "id": "507f1f77bcf86cd799439011",
    "status": "Approved"
  }
}
```

### AI-Powered Rejection
```http
PUT /leaves/{id}/ai-reject
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "rejectionReason": "Insufficient leave balance"
}
```

## 🎨 **Email Template Examples**

### Approval Email Features
- **Personalized Greeting**: "Hi John," vs "Dear John Smith,"
- **Leave Type Messages**: 
  - Vacation: "Time to recharge and enjoy some well-deserved rest! 🌴"
  - Sick Leave: "Take care of your health - that's the most important thing. 🏥"
  - Maternity: "Congratulations on this special time with your family! 👶"
- **Duration-Aware Content**:
  - 1 day: "A short break can be just as refreshing! 😊"
  - 1 week: "A week off is perfect for truly disconnecting! 🔋"
  - Extended: "An extended break - make the most of this valuable time! 🌟"
- **Seasonal Messages**:
  - Winter: "Perfect timing for some winter relaxation! ❄️"
  - Summer: "Summer vibes - enjoy the sunshine! ☀️"

### Rejection Email Features
- **Empathetic Tone**: Understanding and supportive language
- **Clear Explanations**: Professional reasoning for rejection
- **Alternative Suggestions**: Based on rejection reason
- **Next Steps**: Clear guidance on what to do next
- **Contact Information**: Easy way to discuss with HR

## 🔧 **Configuration Options**

### Complete Configuration
```properties
# AI Email Generation Configuration
openai.api.key=your-openai-api-key-here
openai.api.url=https://api.openai.com/v1/chat/completions

# Local AI Configuration
ollama.api.url=http://localhost:11434/api/generate
ollama.model=llama2

# AI Features
ai.email.enabled=true
ai.email.fallback.enabled=true
ai.smart.templates.enabled=true
```

### Minimal Configuration (Smart Templates Only)
```properties
# AI Features - Smart Templates Only
ai.email.enabled=true
ai.smart.templates.enabled=true
```

## 🧪 **Testing the Features**

### Step 1: Run Test Script
```bash
./test_ai_email_features.sh
```

### Step 2: Manual Testing
```bash
# Get authentication token (login to web interface first)
# Get leave request ID
curl -X GET http://localhost:8080/leaves \
  -H "Authorization: Bearer {jwt-token}"

# Test AI approval
curl -X PUT http://localhost:8080/leaves/{leave-id}/ai-approve \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json"

# Test AI rejection
curl -X PUT http://localhost:8080/leaves/{leave-id}/ai-reject \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Testing AI features"}'
```

## 🎯 **Choosing the Right Option**

### For Maximum AI Power: **OpenAI Integration**
- Best for: Organizations wanting cutting-edge AI
- Requirements: OpenAI API key, internet connection
- Cost: Pay-per-use API costs

### For Privacy & Control: **Local AI (Ollama)**
- Best for: Privacy-conscious organizations
- Requirements: Local server resources, technical setup
- Cost: Hardware/infrastructure only

### For Reliability & Simplicity: **Smart Templates**
- Best for: Most organizations, guaranteed reliability
- Requirements: None (built-in)
- Cost: Free

### For Basic Needs: **Fallback Templates**
- Best for: Minimal requirements, emergency fallback
- Requirements: None
- Cost: Free

## 🔄 **How the AI System Works**

### Automatic Fallback Chain
1. **Try OpenAI** (if configured and available)
2. **Try Local AI** (if configured and running)
3. **Use Smart Templates** (always available)
4. **Use Basic Templates** (guaranteed fallback)

### Response Includes AI Method Used
```json
{
  "aiMethod": "OpenAI" | "Local AI" | "Smart Template" | "Basic Template"
}
```

## 🎨 **Customization Options**

### Smart Template Customization
- Modify `SmartEmailTemplateService.java`
- Add new leave type messages
- Customize seasonal greetings
- Add company-specific content

### OpenAI Prompt Customization
- Modify `AIEmailGeneratorService.java`
- Adjust prompts for your organization's tone
- Add company-specific instructions
- Customize for different languages

## 📊 **Benefits**

### For Employees
- **Personalized Communication**: Emails feel personal and relevant
- **Clear Information**: All leave details clearly presented
- **Professional Experience**: High-quality, well-formatted emails
- **Helpful Guidance**: Preparation tips and next steps

### For HR Teams
- **Time Savings**: No manual email composition
- **Consistency**: Professional communication standards maintained
- **Flexibility**: Multiple AI options to choose from
- **Reliability**: Always works with fallback system

### For Organizations
- **Professional Image**: High-quality employee communications
- **Scalability**: Handles any volume of leave requests
- **Customizable**: Adaptable to company culture and needs
- **Cost-Effective**: Multiple pricing options available

## 🛠️ **Advanced Features**

### Multi-Language Support (Future)
- Easy to extend for multiple languages
- Locale-aware content generation
- Cultural adaptation of messaging

### Analytics Integration (Future)
- Track email engagement
- A/B test different AI approaches
- Measure employee satisfaction

### Custom AI Models (Future)
- Train models on company-specific data
- Industry-specific communication styles
- Brand voice consistency

## 🔒 **Security & Privacy**

### Data Handling
- **OpenAI**: Data sent to OpenAI servers (encrypted)
- **Local AI**: All data stays on your servers
- **Smart Templates**: No external data transmission
- **Basic Templates**: No external data transmission

### Compliance
- GDPR compliant options available (Local AI, Smart Templates)
- Data retention policies configurable
- Audit trails for all email generation

## 📞 **Support & Troubleshooting**

### Common Issues
1. **OpenAI API errors**: Check API key and internet connection
2. **Local AI not responding**: Verify Ollama is running
3. **Emails not sending**: Check SMTP configuration
4. **Authentication errors**: Verify JWT token

### Debug Information
- Check application logs for AI method used
- API responses include detailed error information
- Test endpoints available for connectivity checks

## 🚀 **Getting Started**

### Quick Start (Smart Templates Only)
1. No additional setup required
2. Use new AI endpoints: `/ai-approve` and `/ai-reject`
3. Enjoy intelligent email generation immediately

### Advanced Setup (OpenAI)
1. Get OpenAI API key
2. Add to application.properties
3. Restart application
4. Test with AI endpoints

### Privacy Setup (Local AI)
1. Install Ollama
2. Download AI model
3. Configure application.properties
4. Test local AI functionality

Choose the option that best fits your organization's needs, technical capabilities, and privacy requirements!