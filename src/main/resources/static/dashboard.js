// Dashboard JavaScript
class LeaveDashboard {
    constructor() {
        this.baseURL = 'http://localhost:8080';
        this.chart = null;
        this.userRole = this.getUserRole(); // Get user role from URL or session
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.setupRoleBasedUI();
        this.loadDashboardData();
        this.setupTabNavigation();
    }
    
    getUserRole() {
        // Get role from URL parameter
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('role') || 'employee';
    }
    
    setupRoleBasedUI() {
        // Update user role display
        this.updateUserInfo();
        
        // Show/hide HR-specific sections based on role
        if (this.userRole === 'hr') {
            this.showHRFeatures();
            this.loadHRData();
        } else {
            this.hideHRFeatures();
        }
    }
    
    updateUserInfo() {
        const userRoleElement = document.getElementById('userRole');
        if (userRoleElement) {
            userRoleElement.textContent = this.userRole === 'hr' ? 'HR Manager' : 'Employee';
            userRoleElement.className = `user-role ${this.userRole}-role`;
        }
        
        // Load user profile for header
        this.loadHeaderUserInfo();
    }
    
    async loadHeaderUserInfo() {
        try {
            console.log('🔄 Loading header user info...');
            const response = await fetch('/api/user/profile');
            if (response.ok) {
                const userProfile = await response.json();
                console.log('✅ Received user profile:', userProfile);
                
                // Update user name in header
                const userNameElement = document.getElementById('userName');
                if (userNameElement) {
                    userNameElement.textContent = userProfile.fullName || 'User';
                }
                
                // Update user avatar with profile picture or initials
                const userAvatar = document.querySelector('.user-avatar');
                if (userAvatar) {
                    if (userProfile.profilePicture) {
                        // Show profile picture
                        console.log('🖼️ Setting profile picture:', userProfile.profilePicture);
                        userAvatar.style.backgroundImage = `url(${userProfile.profilePicture})`;
                        userAvatar.style.backgroundSize = 'cover';
                        userAvatar.style.backgroundPosition = 'center';
                        userAvatar.textContent = '';
                    } else if (userProfile.fullName) {
                        // Show initials
                        console.log('🔤 Setting initials for:', userProfile.fullName);
                        const initials = userProfile.fullName
                            .split(' ')
                            .map(name => name.charAt(0))
                            .join('')
                            .toUpperCase()
                            .substring(0, 2);
                        userAvatar.textContent = initials;
                        userAvatar.style.backgroundImage = '';
                    }
                }
            } else {
                console.error('❌ Failed to load user profile:', response.status);
            }
        } catch (error) {
            console.error('Error loading header user info:', error);
            // Set default values
            const userAvatar = document.querySelector('.user-avatar');
            if (userAvatar) {
                userAvatar.textContent = 'U';
            }
        }
    }
    

    
    showHRFeatures() {
        // Add HR-specific stats cards
        this.addHRStatsCards();
        
        // Add HR tab if not exists
        this.addHRTab();
        
        // Show approve/reject buttons in history
        document.body.classList.add('hr-user');
    }
    
    hideHRFeatures() {
        // Hide HR-specific elements
        document.body.classList.add('employee-user');
        
        // Hide approve/reject buttons
        const actionButtons = document.querySelectorAll('.action-btn');
        actionButtons.forEach(btn => btn.style.display = 'none');
    }
    
    addHRStatsCards() {
        const statsContainer = document.querySelector('.stats-grid');
        if (statsContainer && !document.getElementById('hrStatsCards')) {
            const hrStatsHTML = `
                <div id="hrStatsCards" class="hr-stats-section">
                    <div class="stat-card hr-stat">
                        <div class="stat-icon">👥</div>
                        <div class="stat-content">
                            <div class="stat-number" id="totalEmployeesCount">0</div>
                            <div class="stat-label">Total Employees</div>
                            <div class="stat-subtitle">Active workforce</div>
                        </div>
                    </div>
                    <div class="stat-card hr-stat">
                        <div class="stat-icon">🏠</div>
                        <div class="stat-content">
                            <div class="stat-number" id="employeesOnLeaveCount">0</div>
                            <div class="stat-label">On Leave</div>
                            <div class="stat-subtitle">Currently away</div>
                        </div>
                    </div>
                    <div class="stat-card hr-stat">
                        <div class="stat-icon">✅</div>
                        <div class="stat-content">
                            <div class="stat-number" id="employeesPresentCount">0</div>
                            <div class="stat-label">Present</div>
                            <div class="stat-subtitle">Available today</div>
                        </div>
                    </div>
                    <div class="stat-card hr-stat">
                        <div class="stat-icon">⏳</div>
                        <div class="stat-content">
                            <div class="stat-number" id="pendingApprovalsCount">0</div>
                            <div class="stat-label">Pending Approvals</div>
                            <div class="stat-subtitle">Awaiting decision</div>
                        </div>
                    </div>
                </div>
            `;
            statsContainer.insertAdjacentHTML('beforeend', hrStatsHTML);
        }
    }
    
    addHRTab() {
        const navTabs = document.querySelector('.nav-tabs');
        const tabContents = document.querySelector('.dashboard-content');
        
        if (navTabs && !document.querySelector('[data-tab="hr-management"]')) {
            // Add HR tab button
            const hrTabButton = document.createElement('button');
            hrTabButton.className = 'nav-tab';
            hrTabButton.setAttribute('data-tab', 'hr-management');
            hrTabButton.innerHTML = `
                <span class="tab-icon">👨‍💼</span>
                HR Management
            `;
            navTabs.appendChild(hrTabButton);
            
            // Add HR tab content
            const hrTabContent = document.createElement('div');
            hrTabContent.id = 'hr-management';
            hrTabContent.className = 'tab-content';
            hrTabContent.innerHTML = `
                <div class="hr-management-section">
                    <h2>HR Management Dashboard</h2>
                    
                    <div class="hr-actions">
                        <div class="hr-section">
                            <h3>📋 Pending Leave Requests</h3>
                            <div id="pendingRequestsList" class="pending-requests-list">
                                <!-- Pending requests will be loaded here -->
                            </div>
                        </div>
                        
                        <div class="hr-section">
                            <h3>📊 Department Overview</h3>
                            <div id="departmentStats" class="department-stats">
                                <!-- Department stats will be loaded here -->
                            </div>
                        </div>
                        
                        <div class="hr-section">
                            <h3>📈 Leave Analytics</h3>
                            <div id="leaveAnalytics" class="leave-analytics">
                                <!-- Analytics will be loaded here -->
                            </div>
                        </div>
                    </div>
                </div>
            `;
            tabContents.appendChild(hrTabContent);
        }
    }

    setupEventListeners() {
        // Form submission
        const leaveForm = document.getElementById('leaveForm');
        if (leaveForm) {
            leaveForm.addEventListener('submit', (e) => this.handleFormSubmit(e));
            // Auto-populate employee name when form is loaded
            this.populateEmployeeName();
        }
    }
    
    async populateEmployeeName() {
        try {
            const response = await fetch('/api/user/profile');
            if (response.ok) {
                const userProfile = await response.json();
                const employeeNameField = document.getElementById('employeeName');
                if (employeeNameField && userProfile.fullName) {
                    employeeNameField.value = userProfile.fullName;
                    employeeNameField.readOnly = true; // Make it read-only since it's auto-populated
                }
            }
        } catch (error) {
            console.error('Error loading employee name:', error);
        }
    }

    setupTabNavigation() {
        const tabButtons = document.querySelectorAll('.nav-tab');
        const tabContents = document.querySelectorAll('.tab-content');

        tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const targetTab = button.getAttribute('data-tab');
                
                // Remove active class from all tabs and contents
                tabButtons.forEach(btn => btn.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));
                
                // Add active class to clicked tab and corresponding content
                button.classList.add('active');
                document.getElementById(targetTab).classList.add('active');
                
                // Load specific data for the tab
                if (targetTab === 'history') {
                    this.loadHistoryData();
                } else if (targetTab === 'hr-management' && this.userRole === 'hr') {
                    this.loadHRData();
                }
            });
        });
    }

    async loadDashboardData() {
        try {
            await Promise.all([
                this.loadStats(),
                this.loadQuarterlyData(),
                this.loadUpcomingLeaves(),
                this.loadTeamOnLeave(),
                this.loadNotifications()
            ]);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        }
    }

    async loadStats() {
        try {
            console.log('📊 Loading user-specific dashboard stats...');
            
            // Use user-specific stats endpoint
            const response = await fetch(`${this.baseURL}/api/dashboard/my-stats`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const stats = await response.json();
            console.log('✅ Loaded user stats:', stats);
            
            // Update stat cards with real data
            document.getElementById('totalLeaveTaken').textContent = `${stats.totalLeaveTaken} days`;
            document.getElementById('approvalRate').textContent = `${stats.approvalRate}%`;
            document.getElementById('pendingRequests').textContent = stats.pendingRequests;
            document.getElementById('teamMembersOnLeave').textContent = stats.teamMembersOnLeave;
            
            // Update remaining days in stat cards with personalized messages
            const remainingDaysElement = document.getElementById('remainingDays');
            if (remainingDaysElement) {
                remainingDaysElement.textContent = `${stats.remainingDays} days remaining this year`;
            }
            
            // Update other subtitle elements
            const statSubtitles = document.querySelectorAll('.stat-subtitle');
            statSubtitles.forEach((element, index) => {
                switch (index) {
                    case 0: // Total Leave Taken
                        element.textContent = `${stats.remainingDays} days remaining this year`;
                        break;
                    case 1: // Approval Rate
                        element.textContent = stats.totalLeaveTaken > 0 ? 
                            `Based on ${stats.totalLeaveTaken + stats.pendingRequests} requests` : 
                            'No requests submitted yet';
                        break;
                    case 2: // Pending Requests
                        element.textContent = stats.pendingRequests > 0 ? 
                            'Awaiting manager approval' : 
                            'No pending requests';
                        break;
                    case 3: // Team Members on Leave
                        element.textContent = stats.teamMembersOnLeave > 0 ? 
                            'Currently on leave today' : 
                            'All team members present';
                        break;
                }
            });
            
        } catch (error) {
            console.error('Error loading stats:', error);
            
            // Show error state in stats
            document.getElementById('totalLeaveTaken').textContent = '-- days';
            document.getElementById('approvalRate').textContent = '--%';
            document.getElementById('pendingRequests').textContent = '--';
            document.getElementById('teamMembersOnLeave').textContent = '--';
            
            const statSubtitles = document.querySelectorAll('.stat-subtitle');
            statSubtitles.forEach(element => {
                element.textContent = 'Error loading data';
                element.style.color = '#ef4444';
            });
        }
    }

    async loadQuarterlyData() {
        try {
            // this line will ask java backend for the data of q1,q2,q3,q4
            const response = await fetch(`${this.baseURL}/api/dashboard/quarterly-data`);
        //  this will call the data
            const data = await response.json();
            
            this.createChart(data);
            
            // Update chart summary
            const totalTaken = Object.values(data.taken).reduce((sum, val) => sum + val, 0);
            const totalRemaining = Object.values(data.remaining).reduce((sum, val) => sum + val, 0);
            
            document.getElementById('chartTotalDays').textContent = totalTaken;
            document.getElementById('chartRemainingDays').textContent = totalRemaining;
            
        } catch (error) {
            console.error('Error loading quarterly data:', error);
        }
    }

    createChart(data) {
        const ctx = document.getElementById('leaveChart').getContext('2d');
        
        if (this.chart) {
            this.chart.destroy();
        }
        
        this.chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Q1', 'Q2', 'Q3', 'Q4'],
                datasets: [
                    {
                        label: 'Leave days taken',
                        data: [data.taken.Q1, data.taken.Q2, data.taken.Q3, data.taken.Q4],
                        backgroundColor: '#4F46E5',
                        borderRadius: 4,
                        barThickness: 40
                    },
                    {
                        label: 'Remaining',
                        data: [data.remaining.Q1, data.remaining.Q2, data.remaining.Q3, data.remaining.Q4],
                        backgroundColor: '#E5E7EB',
                        borderRadius: 4,
                        barThickness: 40
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: '#F1F5F9'
                        }
                    }
                }
            }
        });
    }

    async loadUpcomingLeaves() {
        try {
            console.log('📅 Loading upcoming leaves for current user...');
            
            // Get user's own upcoming leaves from their leave history
            const response = await fetch(`${this.baseURL}/leaves/my-leaves`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const allUserLeaves = await response.json();
            
            // Filter for upcoming approved leaves only
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            
            const upcomingLeaves = allUserLeaves
                .filter(leave => {
                    const startDate = new Date(leave.startDate);
                    return leave.status === 'Approved' && startDate > today;
                })
                .sort((a, b) => new Date(a.startDate) - new Date(b.startDate))
                .slice(0, 5); // Show only next 5
            
            console.log('✅ Found ' + upcomingLeaves.length + ' upcoming leaves');
            
            const container = document.getElementById('upcomingLeaveList');
            container.innerHTML = '';
            
            if (upcomingLeaves.length === 0) {
                container.innerHTML = `
                    <div class="no-data" style="text-align: center; padding: 20px; color: #64748b;">
                        <div style="font-size: 24px; margin-bottom: 10px;">📅</div>
                        <div>No upcoming approved leaves</div>
                        <div style="font-size: 14px; margin-top: 5px;">Submit a leave request to see it here once approved</div>
                    </div>
                `;
                return;
            }
            
            upcomingLeaves.forEach(leave => {
                const leaveItem = document.createElement('div');
                leaveItem.className = 'upcoming-item approved';
                
                // Calculate days until leave starts
                const startDate = new Date(leave.startDate);
                const daysUntil = Math.ceil((startDate - today) / (1000 * 60 * 60 * 24));
                
                leaveItem.innerHTML = `
                    <div class="upcoming-header">
                        <span class="upcoming-title">${leave.leaveType} Leave</span>
                        <span class="upcoming-status">✅ Approved</span>
                    </div>
                    <div class="upcoming-dates">
                        ${this.formatDate(leave.startDate)} - ${this.formatDate(leave.endDate)} (${this.calculateDuration(leave.startDate, leave.endDate)} days)
                    </div>
                    <div class="upcoming-countdown" style="font-size: 12px; color: #3b82f6; margin-top: 4px;">
                        ${daysUntil === 1 ? 'Starts tomorrow' : `Starts in ${daysUntil} days`}
                    </div>
                `;
                
                container.appendChild(leaveItem);
            });
            
        } catch (error) {
            console.error('Error loading upcoming leaves:', error);
            const container = document.getElementById('upcomingLeaveList');
            container.innerHTML = `
                <div class="error" style="text-align: center; padding: 20px; color: #ef4444;">
                    <div style="font-size: 24px; margin-bottom: 10px;">⚠️</div>
                    <div>Error loading upcoming leaves</div>
                    <div style="font-size: 14px; margin-top: 5px;">${error.message}</div>
                </div>
            `;
        }
    }

    async loadTeamOnLeave() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/team-on-leave`);
            const teamMembers = await response.json();
            
            const container = document.getElementById('teamOnLeaveList');
            container.innerHTML = '';
            
            if (teamMembers.length === 0) {
                container.innerHTML = '<p class="text-gray-500">No team members currently on leave</p>';
                return;
            }
            
            teamMembers.forEach(member => {
                const memberItem = document.createElement('div');
                memberItem.className = 'team-member';
                
                memberItem.innerHTML = `
                    <div class="member-avatar"></div>
                    <div class="member-info">
                        <div class="member-name">${member.employeeName}</div>
                        <div class="member-dates">${this.formatDate(member.startDate)} - ${this.formatDate(member.endDate)}</div>
                    </div>
                    <span class="member-type">${member.leaveType}</span>
                `;
                
                container.appendChild(memberItem);
            });
            
        } catch (error) {
            console.error('Error loading team on leave:', error);
        }
    }

    async loadNotifications() {
        try {
            console.log('🔔 Loading user-specific notifications...');
            
            // Get user's own leave requests for notifications
            const response = await fetch(`${this.baseURL}/leaves/my-leaves`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const userLeaves = await response.json();
            
            // Create notifications from user's recent leave requests
            const notifications = userLeaves
                .sort((a, b) => new Date(b.createdAt || b.id) - new Date(a.createdAt || a.id))
                .slice(0, 5) // Show only 5 most recent
                .map(leave => ({
                    id: leave.id,
                    message: this.generateUserNotificationMessage(leave),
                    createdAt: leave.createdAt || new Date().toISOString(),
                    status: leave.status,
                    leaveType: leave.leaveType
                }));
            
            console.log('✅ Generated ' + notifications.length + ' notifications');
            
            const container = document.getElementById('notificationsList');
            container.innerHTML = '';
            
            if (notifications.length === 0) {
                container.innerHTML = `
                    <div class="no-data" style="text-align: center; padding: 20px; color: #64748b;">
                        <div style="font-size: 24px; margin-bottom: 10px;">🔔</div>
                        <div>No recent notifications</div>
                        <div style="font-size: 14px; margin-top: 5px;">Submit leave requests to see updates here</div>
                    </div>
                `;
                return;
            }
            
            notifications.forEach(notification => {
                const notificationItem = document.createElement('div');
                notificationItem.className = 'notification-item';
                
                // Choose icon based on status
                let icon = '📋';
                let iconColor = '#64748b';
                switch (notification.status) {
                    case 'Approved':
                        icon = '✅';
                        iconColor = '#22c55e';
                        break;
                    case 'Rejected':
                        icon = '❌';
                        iconColor = '#ef4444';
                        break;
                    case 'Pending':
                        icon = '⏳';
                        iconColor = '#f59e0b';
                        break;
                }
                
                notificationItem.innerHTML = `
                    <div class="notification-icon" style="color: ${iconColor}; font-size: 18px;">${icon}</div>
                    <div class="notification-content">
                        <div class="notification-text">${notification.message}</div>
                        <div class="notification-time">${this.formatDateTime(notification.createdAt)}</div>
                    </div>
                `;
                
                container.appendChild(notificationItem);
            });
            
        } catch (error) {
            console.error('Error loading notifications:', error);
            const container = document.getElementById('notificationsList');
            container.innerHTML = `
                <div class="error" style="text-align: center; padding: 20px; color: #ef4444;">
                    <div style="font-size: 24px; margin-bottom: 10px;">⚠️</div>
                    <div>Error loading notifications</div>
                    <div style="font-size: 14px; margin-top: 5px;">${error.message}</div>
                </div>
            `;
        }
    }
    
    generateUserNotificationMessage(leave) {
        const leaveType = leave.leaveType || 'Leave';
        switch (leave.status) {
            case 'Pending':
                return `Your ${leaveType} request is pending approval`;
            case 'Approved':
                return `Your ${leaveType} request has been approved`;
            case 'Rejected':
                return `Your ${leaveType} request has been rejected`;
            default:
                return `Your ${leaveType} request status updated`;
        }
    }

    async loadHistoryData() {
        try {
            console.log('📋 Loading leave history for current user...');
            
            // Use user-specific endpoint instead of all leaves
            const response = await fetch(`${this.baseURL}/leaves/my-leaves`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const leaves = await response.json();
            console.log('✅ Loaded ' + leaves.length + ' leave requests for current user');
            
            const tbody = document.getElementById('historyTableBody');
            tbody.innerHTML = '';
            
            if (leaves.length === 0) {
                // Show message when no leaves found
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td colspan="7" style="text-align: center; padding: 20px; color: #64748b;">
                        <div style="display: flex; flex-direction: column; align-items: center; gap: 10px;">
                            <span style="font-size: 24px;">📋</span>
                            <span>No leave requests found</span>
                            <span style="font-size: 14px;">Submit your first leave request using the "Request Leave" tab</span>
                        </div>
                    </td>
                `;
                tbody.appendChild(row);
                return;
            }
            
            // Sort leaves by creation date (newest first)
            leaves.sort((a, b) => new Date(b.createdAt || b.id) - new Date(a.createdAt || a.id));
            
            leaves.forEach(leave => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${leave.employeeName}</td>
                    <td>${this.formatDate(leave.startDate)}</td>
                    <td>${this.formatDate(leave.endDate)}</td>
                    <td>${this.calculateDuration(leave.startDate, leave.endDate)} days</td>
                    <td>${leave.leaveType || 'Annual'}</td>
                    <td><span class="status-badge status-${leave.status.toLowerCase()}">${leave.status}</span></td>
                    <td>
                        ${leave.status === 'Pending' ? `
                            <button class="action-btn cancel-btn" onclick="dashboard.cancelLeaveRequest('${leave.id}')" title="Cancel Request">
                                ❌ Cancel
                            </button>
                        ` : `
                            <span style="color: #64748b; font-size: 14px;">
                                ${leave.status === 'Approved' ? '✅ Approved' : '❌ ' + leave.status}
                            </span>
                        `}
                    </td>
                `;
                tbody.appendChild(row);
            });
            
        } catch (error) {
            console.error('Error loading history data:', error);
            
            // Show error message in table
            const tbody = document.getElementById('historyTableBody');
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 20px; color: #ef4444;">
                        <div style="display: flex; flex-direction: column; align-items: center; gap: 10px;">
                            <span style="font-size: 24px;">⚠️</span>
                            <span>Error loading leave history</span>
                            <span style="font-size: 14px;">${error.message}</span>
                        </div>
                    </td>
                </tr>
            `;
        }
    }

    async handleFormSubmit(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const leaveData = {
            employeeName: formData.get('employeeName'),
            startDate: formData.get('startDate'),
            endDate: formData.get('endDate'),
            reason: formData.get('reason'),
            leaveType: formData.get('leaveType'),
            status: 'Pending'
        };
        
        console.log('📝 Submitting leave request:', leaveData);
        
        // Validate dates
        const startDate = new Date(leaveData.startDate);
        const endDate = new Date(leaveData.endDate);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        if (startDate < today) {
            alert('Start date cannot be in the past.');
            return;
        }
        
        if (endDate < startDate) {
            alert('End date cannot be before start date.');
            return;
        }
        
        try {
            const response = await fetch(`${this.baseURL}/leaves`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(leaveData)
            });
            
            console.log('📤 Response status:', response.status);
            
            if (response.ok) {
                const result = await response.json();
                console.log('✅ Leave request submitted:', result);
                
                // Show success notification
                this.showNotification('Leave request submitted successfully!', 'success');
                
                // Reset form
                e.target.reset();
                
                // Re-populate employee name
                this.populateEmployeeName();
                
                // Refresh dashboard data
                this.loadDashboardData();
            } else {
                const errorText = await response.text();
                console.error('❌ Server error:', response.status, errorText);
                throw new Error(`Server error: ${response.status} - ${errorText}`);
            }
            
        } catch (error) {
            console.error('Error submitting leave request:', error);
            this.showNotification('Error submitting leave request: ' + error.message, 'error');
        }
    }
    
    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;
        
        // Style the notification
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 8px;
            color: white;
            font-weight: 500;
            z-index: 10000;
            animation: slideInRight 0.3s ease-out;
            max-width: 300px;
        `;
        
        // Set background color based on type
        switch (type) {
            case 'success':
                notification.style.background = '#10b981';
                break;
            case 'error':
                notification.style.background = '#ef4444';
                break;
            default:
                notification.style.background = '#3b82f6';
        }
        
        // Add to page
        document.body.appendChild(notification);
        
        // Remove after 4 seconds
        setTimeout(() => {
            notification.style.animation = 'slideOutRight 0.3s ease-in';
            setTimeout(() => {
                if (document.body.contains(notification)) {
                    document.body.removeChild(notification);
                }
            }, 300);
        }, 4000);
    }

    async updateLeaveStatus(leaveId, action) {
        try {
            const response = await fetch(`${this.baseURL}/leaves/${leaveId}/${action}`, {
                method: 'PUT'
            });
            
            if (response.ok) {
                this.showNotification(`Leave request ${action}d successfully!`, 'success');
                this.loadHistoryData();
                this.loadDashboardData();
            } else {
                throw new Error(`Failed to ${action} leave request`);
            }
            
        } catch (error) {
            console.error(`Error ${action}ing leave request:`, error);
            this.showNotification(`Error ${action}ing leave request. Please try again.`, 'error');
        }
    }
    
    async cancelLeaveRequest(leaveId) {
        if (!confirm('Are you sure you want to cancel this leave request?')) {
            return;
        }
        
        try {
            console.log('🗑️ Cancelling leave request:', leaveId);
            
            const response = await fetch(`${this.baseURL}/leaves/${leaveId}`, {
                method: 'DELETE'
            });
            
            if (response.ok) {
                console.log('✅ Leave request cancelled successfully');
                this.showNotification('Leave request cancelled successfully!', 'success');
                this.loadHistoryData(); // Refresh the history table
                this.loadDashboardData(); // Refresh dashboard stats
            } else {
                throw new Error(`Failed to cancel leave request: ${response.status}`);
            }
            
        } catch (error) {
            console.error('Error cancelling leave request:', error);
            this.showNotification('Error cancelling leave request. Please try again.', 'error');
        }
    }

    formatDate(dateString) {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }

    formatDateTime(dateTimeString) {
        if (!dateTimeString) return '';
        const date = new Date(dateTimeString);
        const now = new Date();
        const diffInHours = Math.floor((now - date) / (1000 * 60 * 60));
        
        if (diffInHours < 1) {
            return 'Just now';
        } else if (diffInHours < 24) {
            return `${diffInHours} hours ago`;
        } else {
            const diffInDays = Math.floor(diffInHours / 24);
            return `${diffInDays} days ago`;
        }
    }

    calculateDuration(startDate, endDate) {
        if (!startDate || !endDate) return 0;
        const start = new Date(startDate);
        const end = new Date(endDate);
        const diffTime = Math.abs(end - start);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays + 1; // Include both start and end dates
    }
    
    // ========== HR-SPECIFIC METHODS ==========
    
    async loadHRData() {
        if (this.userRole !== 'hr') return;
        
        try {
            await Promise.all([
                this.loadHRStats(),
                this.loadPendingRequests(),
                this.loadDepartmentStats()
            ]);
        } catch (error) {
            console.error('Error loading HR data:', error);
        }
    }
    
    async loadHRStats() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/employee-stats`);
            const stats = await response.json();
            
            // Update HR-specific stat cards
            document.getElementById('totalEmployeesCount').textContent = stats.totalEmployees;
            document.getElementById('employeesOnLeaveCount').textContent = stats.employeesOnLeave;
            document.getElementById('employeesPresentCount').textContent = stats.employeesPresent;
            document.getElementById('pendingApprovalsCount').textContent = stats.pendingApprovals;
            
        } catch (error) {
            console.error('Error loading HR stats:', error);
        }
    }
    
    async loadPendingRequests() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/pending-requests`);
            const pendingRequests = await response.json();
            
            const container = document.getElementById('pendingRequestsList');
            if (!container) return;
            
            container.innerHTML = '';
            
            if (pendingRequests.length === 0) {
                container.innerHTML = '<p class="no-data">No pending requests</p>';
                return;
            }
            
            pendingRequests.forEach(request => {
                const requestCard = document.createElement('div');
                requestCard.className = 'pending-request-card';
                
                requestCard.innerHTML = `
                    <div class="request-header">
                        <h4>${request.employeeName}</h4>
                        <span class="request-date">${this.formatDate(request.startDate)} - ${this.formatDate(request.endDate)}</span>
                    </div>
                    <div class="request-details">
                        <p><strong>Type:</strong> ${request.leaveType}</p>
                        <p><strong>Duration:</strong> ${request.duration} days</p>
                        <p><strong>Reason:</strong> ${request.reason}</p>
                    </div>
                    <div class="request-actions">
                        <button class="action-btn approve-btn" onclick="dashboard.updateLeaveStatus('${request.id}', 'approve')">
                            ✅ Approve
                        </button>
                        <button class="action-btn reject-btn" onclick="dashboard.updateLeaveStatus('${request.id}', 'reject')">
                            ❌ Reject
                        </button>
                    </div>
                `;
                
                container.appendChild(requestCard);
            });
            
        } catch (error) {
            console.error('Error loading pending requests:', error);
        }
    }
    
    async loadDepartmentStats() {
        try {
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/department-stats`);
            const stats = await response.json();
            
            const container = document.getElementById('departmentStats');
            if (!container) return;
            
            container.innerHTML = '';
            
            const departmentLeaves = stats.departmentLeaves;
            
            Object.entries(departmentLeaves).forEach(([department, leaves]) => {
                const deptCard = document.createElement('div');
                deptCard.className = 'department-card';
                
                deptCard.innerHTML = `
                    <div class="dept-name">${department}</div>
                    <div class="dept-leaves">${leaves} days</div>
                    <div class="dept-bar">
                        <div class="dept-bar-fill" style="width: ${(leaves / 20) * 100}%"></div>
                    </div>
                `;
                
                container.appendChild(deptCard);
            });
            
        } catch (error) {
            console.error('Error loading department stats:', error);
        }
    }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.dashboard = new LeaveDashboard();
});

// Make dashboard available globally for button onclick handlers
window.dashboard = null;

// Global logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        window.location.href = '/logout';
    }
}

// Profile Modal Functions
function showProfile() {
    const modal = document.getElementById('profileModal');
    modal.style.display = 'block';
    loadUserProfile();
}

function closeProfileModal() {
    const modal = document.getElementById('profileModal');
    modal.style.display = 'none';
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('profileModal');
    if (event.target === modal) {
        closeProfileModal();
    }
}

async function loadUserProfile() {
    try {
        // Get current user info from session/authentication
        const response = await fetch('/api/user/profile');
        
        if (response.ok) {
            const userProfile = await response.json();
            
            // Update modal with user data
            document.getElementById('modalFullName').textContent = userProfile.fullName || 'Not provided';
            document.getElementById('modalEmail').textContent = userProfile.email || 'Not provided';
            document.getElementById('modalUsername').textContent = userProfile.username || 'Not provided';
            document.getElementById('modalDepartment').textContent = userProfile.department || 'Not provided';
            document.getElementById('modalRole').textContent = userProfile.role || 'Employee';
            document.getElementById('modalEmployeeCode').textContent = userProfile.employeeCode || 'Not assigned';
            
            // Update profile picture if available
            const modalProfilePicture = document.getElementById('modalProfilePicture');
            if (userProfile.profilePicture) {
                modalProfilePicture.src = userProfile.profilePicture;
            } else {
                // Use default avatar image
                modalProfilePicture.src = '/images/default-avatar.svg';
            }
            
        } else {
            // Show error message if API not available
            document.getElementById('modalFullName').textContent = 'Error loading profile';
            document.getElementById('modalEmail').textContent = 'Please try again later';
            document.getElementById('modalUsername').textContent = 'N/A';
            document.getElementById('modalDepartment').textContent = 'N/A';
            document.getElementById('modalRole').textContent = 'N/A';
            document.getElementById('modalEmployeeCode').textContent = 'N/A';
        }
    } catch (error) {
        console.error('Error loading user profile:', error);
        // Show error message
        document.getElementById('modalFullName').textContent = 'Error loading profile';
        document.getElementById('modalEmail').textContent = 'Please try again later';
        document.getElementById('modalUsername').textContent = 'N/A';
        document.getElementById('modalDepartment').textContent = 'N/A';
        document.getElementById('modalRole').textContent = 'N/A';
        document.getElementById('modalEmployeeCode').textContent = 'N/A';
    }
}

function handleProfilePictureChange(event) {
    const file = event.target.files[0];
    if (file) {
        // Validate file type
        if (!file.type.startsWith('image/')) {
            alert('Please select a valid image file.');
            return;
        }
        
        // Validate file size (max 5MB)
        if (file.size > 5 * 1024 * 1024) {
            alert('File size must be less than 5MB.');
            return;
        }
        
        // Create preview
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('modalProfilePicture').src = e.target.result;
            // Also update the header avatar if needed
            const headerAvatar = document.querySelector('.user-avatar');
            if (headerAvatar) {
                headerAvatar.style.backgroundImage = `url(${e.target.result})`;
                headerAvatar.style.backgroundSize = 'cover';
                headerAvatar.style.backgroundPosition = 'center';
            }
        };
        reader.readAsDataURL(file);
        
        // Here you would typically upload the file to the server
        uploadProfilePicture(file);
    }
}

async function uploadProfilePicture(file) {
    try {
        const formData = new FormData();
        formData.append('profilePicture', file);
        
        const response = await fetch('/api/user/profile-picture', {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            const result = await response.json();
            console.log('Profile picture uploaded successfully:', result);
            
            // Show success message
            if (window.dashboard) {
                window.dashboard.showNotification('Profile picture updated successfully!', 'success');
            }
            
            // Update header avatar with the server response path
            const headerAvatar = document.querySelector('.user-avatar');
            if (headerAvatar && result.profilePicture) {
                headerAvatar.style.backgroundImage = `url(${result.profilePicture})`;
                headerAvatar.style.backgroundSize = 'cover';
                headerAvatar.style.backgroundPosition = 'center';
                headerAvatar.textContent = '';
                console.log('✅ Updated header avatar with:', result.profilePicture);
            }
        } else {
            console.error('Failed to upload profile picture');
            if (window.dashboard) {
                window.dashboard.showNotification('Failed to update profile picture. Please try again.', 'error');
            }
        }
    } catch (error) {
        console.error('Error uploading profile picture:', error);
        if (window.dashboard) {
            window.dashboard.showNotification('Error uploading profile picture. Please try again.', 'error');
        }
    }
}

function saveProfileChanges() {
    // For now, just show a success message
    if (window.dashboard) {
        window.dashboard.showNotification('Profile changes saved successfully!', 'success');
    }
    closeProfileModal();
}

function showNotification(message, type = 'info') {
    // Create notification element
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    // Style the notification
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 8px;
        color: white;
        font-weight: 500;
        z-index: 10000;
        animation: slideInRight 0.3s ease-out;
    `;
    
    // Set background color based on type
    switch (type) {
        case 'success':
            notification.style.background = '#10b981';
            break;
        case 'error':
            notification.style.background = '#ef4444';
            break;
        default:
            notification.style.background = '#3b82f6';
    }
    
    // Add to page
    document.body.appendChild(notification);
    
    // Remove after 3 seconds
    setTimeout(() => {
        notification.style.animation = 'slideOutRight 0.3s ease-in';
        setTimeout(() => {
            document.body.removeChild(notification);
        }, 300);
    }, 3000);
}

// Add CSS for notification animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);