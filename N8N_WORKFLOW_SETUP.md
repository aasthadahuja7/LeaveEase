# N8N Workflow Setup for LeaveEase Email Notifications

## Overview
This document explains how to set up N8N workflows to handle leave approval/rejection email notifications for the LeaveEase application.

## Prerequisites
1. N8N installed and running on `http://localhost:5678`
2. Email service configured (Gmail, Outlook, etc.)
3. LeaveEase application running on `http://localhost:8080`

## Workflow Configuration

### 1. Create Webhook Trigger
- **Node Type**: Webhook
- **Webhook URL**: `http://localhost:5678/webhook/leave-notification`
- **HTTP Method**: POST
- **Authentication**: Optional (use X-N8N-API-Key header)

### 2. Data Processing Node
- **Node Type**: Function or Code
- **Purpose**: Process incoming webhook data and prepare email content

### 3. Email Notification Node
- **Node Type**: Email (Gmail/SMTP)
- **Configuration**: 
  - SMTP Host: smtp.gmail.com
  - Port: 587
  - Username: Your Gmail address
  - Password: App-specific password

## Webhook Payload Structure

### Leave Approved Notification
```json
{
  "type": "LEAVE_APPROVED",
  "action": "APPROVED",
  "employeeName": "John Doe",
  "employeeEmail": "john.doe@company.com",
  "employeeUsername": "johndoe",
  "startDate": "2024-01-15",
  "endDate": "2024-01-20",
  "leaveType": "Annual Leave",
  "duration": 5,
  "reason": "Family vacation",
  "leaveRequestId": "507f1f77bcf86cd799439011",
  "timestamp": "2024-01-10T10:30:00",
  "channels": ["email", "whatsapp", "slack"],
  "priority": "high"
}
```

### Leave Rejected Notification
```json
{
  "type": "LEAVE_REJECTED",
  "action": "REJECTED",
  "employeeName": "John Doe",
  "employeeEmail": "john.doe@company.com",
  "employeeUsername": "johndoe",
  "startDate": "2024-01-15",
  "endDate": "2024-01-20",
  "leaveType": "Annual Leave",
  "duration": 5,
  "reason": "Family vacation",
  "rejectionReason": "Insufficient leave balance",
  "leaveRequestId": "507f1f77bcf86cd799439011",
  "timestamp": "2024-01-10T10:30:00",
  "channels": ["email", "whatsapp", "slack"],
  "priority": "high"
}
```

## Sample N8N Workflow Steps

### Step 1: Webhook Trigger
```javascript
// Webhook receives the payload from LeaveEase application
// No configuration needed, just set the webhook URL
```

### Step 2: Data Processing (Function Node)
```javascript
// Process the webhook data
const data = $input.all()[0].json;

// Determine email template based on action
let emailSubject, emailBody;

if (data.action === 'APPROVED') {
  emailSubject = `✅ Leave Request Approved - ${data.leaveType}`;
  emailBody = `
    Dear ${data.employeeName},
    
    Great news! Your leave request has been approved.
    
    📅 Leave Details:
    • Type: ${data.leaveType}
    • Start Date: ${data.startDate}
    • End Date: ${data.endDate}
    • Duration: ${data.duration} days
    • Reason: ${data.reason}
    
    Please ensure all your work is properly handed over before your leave begins.
    
    Have a wonderful time off!
    
    Best regards,
    HR Team
  `;
} else if (data.action === 'REJECTED') {
  emailSubject = `❌ Leave Request Rejected - ${data.leaveType}`;
  emailBody = `
    Dear ${data.employeeName},
    
    We regret to inform you that your leave request has been rejected.
    
    📅 Leave Details:
    • Type: ${data.leaveType}
    • Start Date: ${data.startDate}
    • End Date: ${data.endDate}
    • Duration: ${data.duration} days
    • Your Reason: ${data.reason}
    
    ❌ Rejection Reason: ${data.rejectionReason}
    
    Please feel free to discuss this with HR or submit a revised request.
    
    Best regards,
    HR Team
  `;
}

return [{
  json: {
    to: data.employeeEmail,
    subject: emailSubject,
    body: emailBody,
    employeeName: data.employeeName,
    leaveType: data.leaveType,
    action: data.action
  }
}];
```

### Step 3: Email Sending (Gmail Node)
- **To**: `{{$json["to"]}}`
- **Subject**: `{{$json["subject"]}}`
- **Email Type**: Text
- **Message**: `{{$json["body"]}}`

### Step 4: Optional - Log/Database Storage
You can add additional nodes to:
- Log the notification to a database
- Send to Slack/Teams
- Send WhatsApp messages
- Create calendar events

## Testing the Integration

### 1. Test N8N Connection
```bash
curl -X GET http://localhost:8080/leaves/test-n8n
```

### 2. Test Leave Approval
```bash
curl -X PUT http://localhost:8080/leaves/{leave-id}/approve \
  -H "Authorization: Bearer {your-jwt-token}" \
  -H "Content-Type: application/json"
```

### 3. Test Leave Rejection
```bash
curl -X PUT http://localhost:8080/leaves/{leave-id}/reject \
  -H "Authorization: Bearer {your-jwt-token}" \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason": "Insufficient leave balance"}'
```

## Configuration in application.properties

Make sure your `application.properties` file has the correct N8N configuration:

```properties
# N8N AI Integration
n8n.webhook.url=http://localhost:5678/webhook/leave-notification
n8n.api.key=your-n8n-api-key-here
```

## Advanced Features

### Multi-Channel Notifications
The payload includes a `channels` array that you can use to send notifications via:
- Email
- WhatsApp (using WhatsApp Business API)
- Slack
- Microsoft Teams
- SMS

### Priority-Based Routing
Use the `priority` field to route urgent notifications differently:
- `high`: Immediate notification
- `medium`: Standard notification
- `low`: Batch notification

### Custom Templates
Create different email templates based on:
- Leave type (Annual, Sick, Emergency)
- Employee role
- Department
- Duration of leave

## Troubleshooting

### Common Issues
1. **N8N not receiving webhooks**: Check firewall and N8N webhook URL
2. **Email not sending**: Verify SMTP credentials and app passwords
3. **Authentication errors**: Check N8N API key configuration

### Debug Endpoints
- Test N8N connection: `GET /leaves/test-n8n`
- Check application logs for webhook responses
- Use N8N's execution log to debug workflow issues

## Security Considerations
1. Use HTTPS in production
2. Implement proper API key authentication
3. Validate webhook payloads
4. Rate limit webhook endpoints
5. Log all notification attempts for audit purposes