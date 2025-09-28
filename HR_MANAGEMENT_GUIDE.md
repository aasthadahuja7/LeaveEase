# HR Management Dashboard Guide

## 🎯 Overview

The HR Management Dashboard allows HR managers to approve and reject leave requests with detailed reasons. This ensures transparency and helps employees understand why their requests were rejected.

## 🚀 Getting Started

### 1. Access HR Dashboard
- Start the application: `./mvnw spring-boot:run`
- Open the dashboard with HR role: `http://localhost:8080/dashboard.html?role=hr`
- You'll see the "HR Management" tab in the navigation

### 2. HR Dashboard Features

#### 📊 HR Statistics Cards
- **Total Employees**: Shows the total number of employees in the system
- **Pending Requests**: Number of leave requests awaiting approval
- **Approved This Month**: Number of approved requests in the current month
- **Rejected This Month**: Number of rejected requests in the current month

#### 📋 Pending Leave Requests Section
- Shows all leave requests with "Pending" status
- Each request displays:
  - Employee name
  - Leave type and duration
  - Start and end dates
  - Reason for leave
  - Approve/Reject buttons

#### 📊 All Leave Requests Section
- Shows all leave requests (Pending, Approved, Rejected)
- Filter by status using the dropdown
- Displays rejection reasons for rejected requests
- Shows approval/rejection actions for pending requests

## ✅ How to Approve Leave Requests

1. **Navigate to HR Management Tab**
   - Click on the "HR Management" tab in the navigation

2. **View Pending Requests**
   - Go to the "Pending Leave Requests" section
   - Review the leave request details

3. **Approve the Request**
   - Click the "✅ Approve" button
   - Confirm the action in the popup dialog
   - The request will be marked as "Approved"

## ❌ How to Reject Leave Requests

1. **Navigate to HR Management Tab**
   - Click on the "HR Management" tab in the navigation

2. **View Pending Requests**
   - Go to the "Pending Leave Requests" section
   - Review the leave request details

3. **Reject the Request**
   - Click the "❌ Reject" button
   - A modal will open showing the request details

4. **Provide Rejection Reason**
   - Enter a detailed reason for rejection (minimum 10 characters)
   - The reason should be clear and constructive
   - Click "Reject Leave" to confirm

5. **Confirmation**
   - The request will be marked as "Rejected"
   - The rejection reason will be visible to the employee

## 👀 Employee View of Rejection Reasons

When employees view their leave history:
- Rejected requests show the rejection reason below the status
- The reason appears in red text for easy identification
- This helps employees understand why their request was rejected

## 🔧 Technical Implementation

### Backend Changes
- **LeaveRequest Model**: Added `rejectionReason` field
- **LeaveController**: Updated to handle rejection reasons
- **DashboardController**: Enhanced HR endpoints to include rejection reasons

### Frontend Changes
- **Dashboard HTML**: Added HR management tab and rejection modal
- **Dashboard CSS**: Styled HR components and rejection modal
- **Dashboard JavaScript**: Implemented HR approval/rejection logic

### API Endpoints
- `PUT /leaves/{id}/approve` - Approve a leave request
- `PUT /leaves/{id}/reject` - Reject a leave request with reason
- `PUT /leaves/{id}/hr-action` - HR action with detailed information
- `GET /api/dashboard/hr/employee-stats` - HR statistics
- `GET /api/dashboard/hr/pending-requests` - Pending requests
- `GET /api/dashboard/hr/all-requests` - All requests with rejection reasons

## 📱 Responsive Design

The HR dashboard is fully responsive:
- Works on desktop, tablet, and mobile devices
- HR stats cards stack vertically on smaller screens
- Request cards adapt to screen size
- Modal dialogs are mobile-friendly

## 🎨 User Experience Features

### Visual Feedback
- Success notifications for approvals/rejections
- Error messages for failed operations
- Loading states during API calls
- Color-coded status badges

### Validation
- Rejection reason is required
- Minimum 10 characters for rejection reason
- Confirmation dialogs for important actions
- Form validation for all inputs

### Accessibility
- Clear button labels and icons
- Proper contrast ratios
- Keyboard navigation support
- Screen reader friendly

## 🧪 Testing the Functionality

1. **Start the Application**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Test HR Dashboard**
   - Open: `http://localhost:8080/dashboard.html?role=hr`
   - Create leave requests as different employees
   - Test approval and rejection with reasons

3. **Run Test Script**
   ```bash
   ./test_hr_functionality.sh
   ```

## 🔒 Security Considerations

- Only users with HR role can access HR management features
- Rejection reasons are validated and sanitized
- All actions are logged for audit purposes
- API endpoints require proper authentication

## 🚀 Future Enhancements

Potential improvements for the HR management system:
- Email notifications for approvals/rejections
- Bulk approval/rejection functionality
- Advanced filtering and search
- Leave request templates
- Approval workflow with multiple levels
- Integration with calendar systems
- Reporting and analytics dashboard

## 📞 Support

If you encounter any issues with the HR management functionality:
1. Check the browser console for JavaScript errors
2. Verify the application is running on port 8080
3. Ensure you're accessing the dashboard with `?role=hr` parameter
4. Check the application logs for backend errors

---

**Happy HR Management! 🎉** 