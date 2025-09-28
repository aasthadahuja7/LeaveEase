// Dashboard JavaScript
class LeaveDashboard {
    constructor() {
        this.baseURL = 'http://localhost:8080';
        this.chart = null;
        this.userRole = this.getUserRole(); // Get user role from URL or session
        this.currentDate = new Date();
        this.calendarData = {
            leaveDays: [],
            lateDays: []
        };
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
        
        // Update dashboard title based on role
        this.updateDashboardTitle();
        
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
    
    updateDashboardTitle() {
        const dashboardTitle = document.getElementById('dashboardTitle');
        if (dashboardTitle) {
            if (this.userRole === 'hr') {
                dashboardTitle.textContent = 'HR Management Dashboard';
                dashboardTitle.classList.add('hr-title');
                // Update page title for HR
                document.title = 'HR Management Dashboard - Leave App';
            } else {
                dashboardTitle.textContent = 'LeaveEase Dashboard';
                dashboardTitle.classList.remove('hr-title');
                // Update page title for employees
                document.title = 'LeaveEase Dashboard';
            }
        }
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
        // Show HR tab and add hr-user class to body
        document.body.classList.add('hr-user');
        
        // Load HR data
        this.loadHRData();
        
        // Update dashboard title for HR
        this.updateDashboardTitle('HR Management');
        
        // Show the HR Management tab
        const hrTab = document.querySelector('.nav-tab[data-tab="hr-management"]');
        if (hrTab) {
            hrTab.style.display = 'flex';
        }
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
        
        // Calendar navigation
        const prevMonthBtn = document.getElementById('prevMonth');
        const nextMonthBtn = document.getElementById('nextMonth');
        
        if (prevMonthBtn) {
            prevMonthBtn.addEventListener('click', () => this.navigateMonth(-1));
        }
        
        if (nextMonthBtn) {
            nextMonthBtn.addEventListener('click', () => this.navigateMonth(1));
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
                } else if (targetTab === 'calendar') {
                    this.loadCalendarData();
                } else if (targetTab === 'hr-management' && this.userRole === 'hr') {
                    this.loadHRManagementData();
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

            // Load HR-specific data if user is HR
            if (this.getUserRole() === 'hr') {
                await this.loadLateAttendanceChart();
            }
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
            console.log('📊 Loading quarterly leave data for current user...');
            
            const response = await fetch(`${this.baseURL}/api/dashboard/quarterly-data`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const data = await response.json();
            console.log('✅ Received quarterly data:', data);
            
            this.createChart(data);
            
            // Update chart summary with real user data
            const totalTaken = data.totalTakenThisYear || 0;
            const totalRemaining = data.totalRemainingThisYear || 0;
            const annualAllowance = data.annualAllowance || 25;
            
            document.getElementById('chartTotalDays').textContent = totalTaken;
            document.getElementById('chartRemainingDays').textContent = totalRemaining;
            
            // Update the chart subtitle with real data
            const chartSubtitle = document.querySelector('.chart-subtitle');
            if (chartSubtitle) {
                chartSubtitle.textContent = `${totalTaken} days used of ${annualAllowance} annual allowance`;
            }
            
            console.log(`📈 Chart updated: ${totalTaken} taken, ${totalRemaining} remaining out of ${annualAllowance} total`);
            
        } catch (error) {
            console.error('❌ Error loading quarterly data:', error);
            
            // Show error state in chart
            document.getElementById('chartTotalDays').textContent = '--';
            document.getElementById('chartRemainingDays').textContent = '--';
            
            const chartSubtitle = document.querySelector('.chart-subtitle');
            if (chartSubtitle) {
                chartSubtitle.textContent = 'Error loading leave data';
                chartSubtitle.style.color = '#ef4444';
            }
        }
    }

    createChart(data) {
        const ctx = document.getElementById('leaveChart').getContext('2d');
        
        if (this.chart) {
            this.chart.destroy();
        }
        
        // Get current year for better labels
        const currentYear = new Date().getFullYear();
        
        this.chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: [
                    `Q1 ${currentYear}`,
                    `Q2 ${currentYear}`,
                    `Q3 ${currentYear}`,
                    `Q4 ${currentYear}`
                ],
                datasets: [
                    {
                        label: 'Days Taken',
                        data: [
                            data.taken.Q1 || 0,
                            data.taken.Q2 || 0,
                            data.taken.Q3 || 0,
                            data.taken.Q4 || 0
                        ],
                        backgroundColor: '#4F46E5',
                        borderRadius: 6,
                        barThickness: 35
                    },
                    {
                        label: 'Available',
                        data: [
                            data.remaining.Q1 || 0,
                            data.remaining.Q2 || 0,
                            data.remaining.Q3 || 0,
                            data.remaining.Q4 || 0
                        ],
                        backgroundColor: '#E5E7EB',
                        borderRadius: 6,
                        barThickness: 35
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
            console.log('👥 Loading team members on leave TODAY...');
            
            const response = await fetch(`${this.baseURL}/api/dashboard/team-on-leave`);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const teamMembers = await response.json();
            console.log('✅ Found ' + teamMembers.length + ' team members on leave today');
            
            const container = document.getElementById('teamOnLeaveList');
            container.innerHTML = '';
            
            if (teamMembers.length === 0) {
                container.innerHTML = `
                    <div class="no-data" style="text-align: center; padding: 20px; color: #64748b;">
                        <div style="font-size: 24px; margin-bottom: 10px;">👥</div>
                        <div>All team members are present today</div>
                        <div style="font-size: 14px; margin-top: 5px;">No one is on leave today</div>
                    </div>
                `;
                return;
            }
            
            teamMembers.forEach(member => {
                const memberItem = document.createElement('div');
                memberItem.className = 'team-member';
                
                // Calculate how many days into their leave they are
                const startDate = new Date(member.startDate);
                const endDate = new Date(member.endDate);
                const today = new Date();
                
                const totalLeaveDays = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;
                const daysIntoLeave = Math.ceil((today - startDate) / (1000 * 60 * 60 * 24)) + 1;
                const daysRemaining = Math.ceil((endDate - today) / (1000 * 60 * 60 * 24));
                
                let statusText = '';
                if (daysRemaining === 0) {
                    statusText = 'Last day of leave';
                } else if (daysRemaining === 1) {
                    statusText = 'Returns tomorrow';
                } else {
                    statusText = `Returns in ${daysRemaining} days`;
                }
                
                memberItem.innerHTML = `
                    <div class="member-avatar" style="background: linear-gradient(135deg, #4f46e5, #7c3aed); color: white; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; font-size: 14px;">
                        ${member.employeeName.split(' ').map(n => n[0]).join('').substring(0, 2)}
                    </div>
                    <div class="member-info" style="flex: 1;">
                        <div class="member-name" style="font-weight: 600; color: #1f2937; margin-bottom: 2px;">${member.employeeName}</div>
                        <div class="member-type" style="color: #6b7280; font-size: 13px; margin-bottom: 2px;">${member.leaveType} Leave</div>
                        <div class="member-status" style="color: #3b82f6; font-size: 12px; font-weight: 500;">${statusText}</div>
                    </div>
                    <div class="member-duration" style="text-align: right; color: #6b7280; font-size: 12px;">
                        <div>Day ${daysIntoLeave}/${totalLeaveDays}</div>
                        <div style="margin-top: 2px; color: #ef4444; font-weight: 500;">🔴 On Leave</div>
                    </div>
                `;
                
                container.appendChild(memberItem);
            });
            
        } catch (error) {
            console.error('Error loading team on leave:', error);
            const container = document.getElementById('teamOnLeaveList');
            container.innerHTML = `
                <div class="error" style="text-align: center; padding: 20px; color: #ef4444;">
                    <div style="font-size: 24px; margin-bottom: 10px;">⚠️</div>
                    <div>Error loading team data</div>
                    <div style="font-size: 14px; margin-top: 5px;">${error.message}</div>
                </div>
            `;
        }
    }

    async loadNotifications() {
        try {
            console.log('🔔 Loading user-specific notifications...');
            
            // Get user's own leave requests for notifications
            const leaveResponse = await fetch(`${this.baseURL}/leaves/my-leaves`);
            
            if (!leaveResponse.ok) {
                throw new Error(`HTTP ${leaveResponse.status}: ${leaveResponse.statusText}`);
            }
            
            const userLeaves = await leaveResponse.json();
            
            // Get user's late attendance records for notifications
            const lateResponse = await fetch(`${this.baseURL}/api/late-attendance/my-late-records`);
            let lateRecords = [];
            
            if (lateResponse.ok) {
                const lateResult = await lateResponse.json();
                if (lateResult.success && lateResult.data) {
                    lateRecords = lateResult.data;
                }
            }
            
            // Create notifications from user's recent leave requests
            const leaveNotifications = userLeaves
                .sort((a, b) => new Date(b.createdAt || b.id) - new Date(a.createdAt || a.id))
                .slice(0, 3) // Show only 3 most recent leave notifications
                .map(leave => ({
                    id: leave.id,
                    message: this.generateUserNotificationMessage(leave),
                    createdAt: leave.createdAt || new Date().toISOString(),
                    status: leave.status,
                    leaveType: leave.leaveType,
                    type: 'leave'
                }));
            
            // Create notifications from user's recent late attendance records
            const lateNotifications = lateRecords
                .sort((a, b) => new Date(b.date) - new Date(a.date))
                .slice(0, 2) // Show only 2 most recent late notifications
                .map(record => ({
                    id: record.id,
                    message: `You were marked as late on ${new Date(record.date).toLocaleDateString()}. Reason: ${record.reason || 'Not specified'}`,
                    createdAt: record.date,
                    status: 'Late',
                    type: 'late'
                }));
            
            // Combine and sort all notifications by date
            const allNotifications = [...leaveNotifications, ...lateNotifications]
                .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                .slice(0, 5); // Show only 5 most recent total notifications
            
            console.log('✅ Generated ' + allNotifications.length + ' notifications');
            
            const container = document.getElementById('notificationsList');
            container.innerHTML = '';
            
            if (allNotifications.length === 0) {
                container.innerHTML = `
                    <div class="no-data" style="text-align: center; padding: 20px; color: #64748b;">
                        <div style="font-size: 24px; margin-bottom: 10px;">🔔</div>
                        <div>No recent notifications</div>
                        <div style="font-size: 14px; margin-top: 5px;">Submit leave requests to see updates here</div>
                    </div>
                `;
                return;
            }
            
            allNotifications.forEach(notification => {
                const notificationItem = document.createElement('div');
                notificationItem.className = 'notification-item';
                
                // Choose icon based on type and status
                let icon = '📋';
                let iconColor = '#64748b';
                
                if (notification.type === 'late') {
                    icon = '⏰';
                    iconColor = '#f59e0b';
                } else {
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
                    <td colspan="6" style="text-align: center; padding: 20px; color: #64748b;">
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
                    <td>${this.formatDate(leave.startDate)}</td>
                    <td>${this.formatDate(leave.endDate)}</td>
                    <td>${this.calculateDuration(leave.startDate, leave.endDate)} days</td>
                    <td>${leave.leaveType || 'Annual'}</td>
                    <td>
                        <span class="status-badge status-${leave.status.toLowerCase()}">${leave.status}</span>
                        ${leave.rejectionReason ? `
                            <br><small style="color: #dc2626; font-size: 11px; margin-top: 4px; display: block;">
                                ❌ ${leave.rejectionReason}
                            </small>
                        ` : ''}
                    </td>
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
                    <td colspan="6" style="text-align: center; padding: 20px; color: #ef4444;">
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
    
    // HR-specific methods
    async approveLeaveRequest(leaveId) {
        if (!confirm('Are you sure you want to approve this leave request? An email will be sent directly from your email to the employee.')) {
            return;
        }
        
        try {
            console.log('✅ Approving leave request with HR direct email:', leaveId);
            
            // Use the new HR direct approval endpoint
            const response = await fetch(`${this.baseURL}/leaves/${leaveId}/hr-approve`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            const result = await response.json();
            
            if (result.success) {
                console.log('✅ Leave request approved successfully');
                console.log('📧 Email method:', result.emailMethod);
                console.log('📧 From:', result.fromEmail);
                console.log('📧 To:', result.toEmail);
                
                let message = 'Leave request approved successfully!';
                if (result.emailMethod === 'HR Direct Email') {
                    message += ` Email sent from your account (${result.fromEmail}) to ${result.toEmail}`;
                } else {
                    message += ` Email sent via system (configure your email for direct communication)`;
                }
                
                this.showNotification(message, 'success');
                this.loadHRData(); // Refresh HR data
            } else {
                throw new Error(result.message || 'Failed to approve leave request');
            }
            
        } catch (error) {
            console.error('Error approving leave request:', error);
            this.showNotification('Error approving leave request. Please try again.', 'error');
        }
    }
    
    showRejectionModal(leaveId) {
        // Find the request details
        const request = this.allHRRequests?.find(r => r.id === leaveId);
        if (!request) {
            this.showNotification('Request not found', 'error');
            return;
        }
        
        // Store the leave ID for the rejection
        this.currentRejectionLeaveId = leaveId;
        
        // Populate the modal with request details
        const detailsContainer = document.getElementById('rejectionRequestDetails');
        detailsContainer.innerHTML = `
            <div style="background: #f8fafc; border-radius: 8px; padding: 16px; margin-bottom: 16px;">
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 12px;">
                    <div>
                        <div style="font-size: 12px; color: #64748b; font-weight: 500;">Employee</div>
                        <div style="font-size: 14px; color: #1e293b; font-weight: 600;">${request.employeeName}</div>
                    </div>
                    <div>
                        <div style="font-size: 12px; color: #64748b; font-weight: 500;">Leave Type</div>
                        <div style="font-size: 14px; color: #1e293b; font-weight: 600;">${request.leaveType}</div>
                    </div>
                    <div>
                        <div style="font-size: 12px; color: #64748b; font-weight: 500;">Duration</div>
                        <div style="font-size: 14px; color: #1e293b; font-weight: 600;">${request.duration} days</div>
                    </div>
                    <div>
                        <div style="font-size: 12px; color: #64748b; font-weight: 500;">Dates</div>
                        <div style="font-size: 14px; color: #1e293b; font-weight: 600;">${this.formatDate(request.startDate)} - ${this.formatDate(request.endDate)}</div>
                    </div>
                </div>
                <div style="margin-top: 12px;">
                    <div style="font-size: 12px; color: #64748b; font-weight: 500;">Reason for Leave</div>
                    <div style="font-size: 14px; color: #1e293b; margin-top: 4px;">${request.reason}</div>
                </div>
            </div>
        `;
        
        // Clear previous rejection reason
        document.getElementById('rejectionReason').value = '';
        
        // Show the modal
        document.getElementById('hrRejectionModal').style.display = 'block';
    }
    
    async confirmRejectLeave() {
        const rejectionReason = document.getElementById('rejectionReason').value.trim();
        
        if (!rejectionReason) {
            alert('Please provide a reason for rejection.');
            return;
        }
        
        if (rejectionReason.length < 10) {
            alert('Please provide a more detailed reason for rejection (at least 10 characters).');
            return;
        }
        
        try {
            console.log('❌ Rejecting leave request with HR direct email:', this.currentRejectionLeaveId);
            console.log('Reason:', rejectionReason);
            
            // Use the new HR direct rejection endpoint
            const response = await fetch(`${this.baseURL}/leaves/${this.currentRejectionLeaveId}/hr-reject`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    rejectionReason: rejectionReason
                })
            });
            
            const result = await response.json();
            
            if (result.success) {
                console.log('✅ Leave request rejected successfully');
                console.log('📧 Email method:', result.emailMethod);
                console.log('📧 From:', result.fromEmail);
                console.log('📧 To:', result.toEmail);
                
                let message = 'Leave request rejected successfully!';
                if (result.emailMethod === 'HR Direct Email') {
                    message += ` Email sent from your account (${result.fromEmail}) to ${result.toEmail}`;
                } else {
                    message += ` Email sent via system (configure your email for direct communication)`;
                }
                
                this.showNotification(message, 'success');
                this.closeHRRejectionModal();
                this.loadHRData(); // Refresh HR data
            } else {
                throw new Error(result.message || 'Failed to reject leave request');
            }
            
        } catch (error) {
            console.error('Error rejecting leave request:', error);
            this.showNotification('Error rejecting leave request. Please try again.', 'error');
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
                this.loadAllRequests(),
                this.loadLateAttendanceData(),
                this.loadHRHistoryData()
            ]);
        } catch (error) {
            console.error('Error loading HR data:', error);
        }
    }
    
    async loadHRStats() {
        try {
            console.log('📊 Loading HR stats...');
            
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/employee-stats`);
            const stats = await response.json();
            
            console.log('✅ Loaded HR stats:', stats);
            
            // Update HR-specific stat cards
            document.getElementById('hrTotalEmployees').textContent = stats.totalEmployees || 0;
            document.getElementById('hrPendingRequests').textContent = stats.pendingApprovals || 0;
            document.getElementById('hrApprovedRequests').textContent = stats.approvedRequests || 0;
            document.getElementById('hrRejectedRequests').textContent = stats.rejectedRequests || 0;
            
        } catch (error) {
            console.error('Error loading HR stats:', error);
        }
    }
    

    
    async loadPendingRequests() {
        try {
            console.log('📋 Loading pending requests for HR...');
            
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/pending-requests`);
            const pendingRequests = await response.json();
            
            console.log('✅ Loaded pending requests:', pendingRequests);
            
            const container = document.getElementById('hrPendingRequestsList');
            if (!container) return;
            
            container.innerHTML = '';
            
            if (pendingRequests.length === 0) {
                container.innerHTML = `
                    <div class="no-data" style="text-align: center; padding: 20px; color: #64748b;">
                        <div style="font-size: 24px; margin-bottom: 10px;">✅</div>
                        <div>No pending leave requests</div>
                        <div style="font-size: 14px; margin-top: 5px;">All requests have been processed</div>
                    </div>
                `;
                return;
            }
            
            pendingRequests.forEach(request => {
                const requestCard = document.createElement('div');
                requestCard.className = 'hr-request-card';
                
                requestCard.innerHTML = `
                    <div class="hr-request-header">
                        <div class="hr-request-employee">${request.employeeName}</div>
                        <span class="hr-request-status pending">Pending</span>
                    </div>
                    <div class="hr-request-details">
                        <div class="hr-request-detail">
                            <div class="hr-request-detail-label">Start Date</div>
                            <div class="hr-request-detail-value">${this.formatDate(request.startDate)}</div>
                        </div>
                        <div class="hr-request-detail">
                            <div class="hr-request-detail-label">End Date</div>
                            <div class="hr-request-detail-value">${this.formatDate(request.endDate)}</div>
                        </div>
                        <div class="hr-request-detail">
                            <div class="hr-request-detail-label">Duration</div>
                            <div class="hr-request-detail-value">${request.duration} days</div>
                        </div>
                        <div class="hr-request-detail">
                            <div class="hr-request-detail-label">Leave Type</div>
                            <div class="hr-request-detail-value">${request.leaveType}</div>
                        </div>
                    </div>
                    <div class="hr-request-reason">
                        <div class="hr-request-reason-label">Reason for Leave</div>
                        <div class="hr-request-reason-text">${request.reason}</div>
                    </div>
                    <div class="hr-request-actions">
                        <button class="hr-action-btn hr-approve-btn" onclick="dashboard.approveLeaveRequest('${request.id}')">
                            ✅ Approve
                        </button>
                        <button class="hr-action-btn hr-reject-btn" onclick="dashboard.showRejectionModal('${request.id}')">
                            ❌ Reject
                        </button>
                    </div>
                `;
                
                container.appendChild(requestCard);
            });
            
        } catch (error) {
            console.error('Error loading pending requests:', error);
            const container = document.getElementById('hrPendingRequestsList');
            if (container) {
                container.innerHTML = `
                    <div class="error" style="text-align: center; padding: 20px; color: #ef4444;">
                        <div style="font-size: 24px; margin-bottom: 10px;">⚠️</div>
                        <div>Error loading pending requests</div>
                        <div style="font-size: 14px; margin-top: 5px;">${error.message}</div>
                    </div>
                `;
            }
        }
    }
    

    
    async loadAllRequests() {
        try {
            console.log('📊 Loading all requests for HR...');
            
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/all-requests`);
            const allRequests = await response.json();
            
            console.log('✅ Loaded all requests:', allRequests);
            
            this.allHRRequests = allRequests; // Store for filtering
            this.renderAllRequests(allRequests);
            
        } catch (error) {
            console.error('Error loading all requests:', error);
            const container = document.getElementById('hrAllRequestsList');
            if (container) {
                container.innerHTML = `
                    <div class="error" style="text-align: center; padding: 20px; color: #ef4444;">
                        <div style="font-size: 24px; margin-bottom: 10px;">⚠️</div>
                        <div>Error loading all requests</div>
                        <div style="font-size: 14px; margin-top: 5px;">${error.message}</div>
                    </div>
                `;
            }
        }
    }
    

    
    renderAllRequests(requests) {
        const container = document.getElementById('hrAllRequestsList');
        if (!container) return;
        
        container.innerHTML = '';
        
        if (requests.length === 0) {
            container.innerHTML = `
                <div class="no-data" style="text-align: center; padding: 20px; color: #64748b;">
                    <div style="font-size: 24px; margin-bottom: 10px;">📋</div>
                    <div>No leave requests found</div>
                    <div style="font-size: 14px; margin-top: 5px;">Employees will appear here when they submit requests</div>
                </div>
            `;
            return;
        }
        
        requests.forEach(request => {
            const requestCard = document.createElement('div');
            requestCard.className = 'hr-request-card';
            
            const statusClass = request.status.toLowerCase();
            const statusIcon = this.getStatusIcon(request.status);
            
            requestCard.innerHTML = `
                <div class="hr-request-header">
                    <div class="hr-request-employee">${request.employeeName}</div>
                    <span class="hr-request-status ${statusClass}">${statusIcon} ${request.status}</span>
                </div>
                <div class="hr-request-details">
                    <div class="hr-request-detail">
                        <div class="hr-request-detail-label">Start Date</div>
                        <div class="hr-request-detail-value">${this.formatDate(request.startDate)}</div>
                    </div>
                    <div class="hr-request-detail">
                        <div class="hr-request-detail-label">End Date</div>
                        <div class="hr-request-detail-value">${this.formatDate(request.endDate)}</div>
                    </div>
                    <div class="hr-request-detail">
                        <div class="hr-request-detail-label">Duration</div>
                        <div class="hr-request-detail-value">${request.duration} days</div>
                    </div>
                    <div class="hr-request-detail">
                        <div class="hr-request-detail-label">Leave Type</div>
                        <div class="hr-request-detail-value">${request.leaveType}</div>
                    </div>
                </div>
                <div class="hr-request-reason">
                    <div class="hr-request-reason-label">Reason for Leave</div>
                    <div class="hr-request-reason-text">${request.reason}</div>
                </div>
                ${request.rejectionReason ? `
                    <div class="hr-request-rejection-reason">
                        <div class="hr-request-rejection-reason-label">Rejection Reason</div>
                        <div class="hr-request-rejection-reason-text">${request.rejectionReason}</div>
                    </div>
                ` : ''}
                <div class="hr-request-actions">
                    ${request.status === 'Pending' ? `
                        <button class="hr-action-btn hr-approve-btn" onclick="dashboard.approveLeaveRequest('${request.id}')">
                            ✅ Approve
                        </button>
                        <button class="hr-action-btn hr-reject-btn" onclick="dashboard.showRejectionModal('${request.id}')">
                            ❌ Reject
                        </button>
                    ` : `
                        <span style="color: #64748b; font-size: 14px;">
                            ${request.status === 'Approved' ? '✅ Approved' : '❌ Rejected'}
                        </span>
                    `}
                </div>
            `;
            
            container.appendChild(requestCard);
        });
        

    }
    
    getStatusIcon(status) {
        switch (status) {
            case 'Pending': return '⏳';
            case 'Approved': return '✅';
            case 'Rejected': return '❌';
            default: return '📋';
        }
    }
    
    filterHRRequests() {
        const filterValue = document.getElementById('hrStatusFilter').value;
        let filteredRequests = this.allHRRequests || [];
        
        if (filterValue !== 'all') {
            filteredRequests = filteredRequests.filter(request => request.status === filterValue);
        }
        
        this.renderAllRequests(filteredRequests);
    }
    
    // ========== CALENDAR METHODS ==========
    
    async loadCalendarData() {
        try {
            console.log('📅 Loading calendar data...');
            
            // Load user's leave data
            await this.loadUserLeaveData();
            
            // Load user's attendance data (late days) - currently not implemented
            await this.loadUserAttendanceData();
            
            // Render the calendar
            this.renderCalendar();
            
            // Update calendar summary
            this.updateCalendarSummary();
            
        } catch (error) {
            console.error('Error loading calendar data:', error);
        }
    }
    
    async loadUserLeaveData() {
        try {
            const response = await fetch(`${this.baseURL}/leaves/my-leaves`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const leaves = await response.json();
            
            // Extract all leave days from approved requests
            this.calendarData.leaveDays = [];
            
            leaves.forEach(leave => {
                if (leave.status === 'Approved' && leave.startDate && leave.endDate) {
                    const startDate = new Date(leave.startDate);
                    const endDate = new Date(leave.endDate);
                    
                    // Add all days between start and end date
                    for (let date = new Date(startDate); date <= endDate; date.setDate(date.getDate() + 1)) {
                        this.calendarData.leaveDays.push({
                            date: new Date(date),
                            type: leave.leaveType,
                            reason: leave.reason
                        });
                    }
                }
            });
            
            console.log('✅ Loaded leave days:', this.calendarData.leaveDays.length);
            
        } catch (error) {
            console.error('Error loading leave data:', error);
            this.calendarData.leaveDays = [];
        }
    }
    
    async loadUserAttendanceData() {
        try {
            console.log('⏰ Loading user attendance data...');
            
            const response = await fetch(`${this.baseURL}/api/late-attendance/my-late-records`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const result = await response.json();
            
            if (result.success && result.data) {
                // Convert late attendance records to calendar format
                this.calendarData.lateDays = result.data.map(record => ({
                    date: new Date(record.date),
                    reason: record.reason,
                    notes: record.notes,
                    markedBy: record.markedBy
                }));
                
                console.log('✅ Loaded late days:', this.calendarData.lateDays.length);
            } else {
                console.log('ℹ️ No late attendance records found');
                this.calendarData.lateDays = [];
            }
            
        } catch (error) {
            console.error('Error loading attendance data:', error);
            this.calendarData.lateDays = [];
        }
    }
    
    renderCalendar() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        
        // Update month/year display
        const monthNames = [
            'January', 'February', 'March', 'April', 'May', 'June',
            'July', 'August', 'September', 'October', 'November', 'December'
        ];
        
        document.getElementById('currentMonth').textContent = monthNames[month];
        document.getElementById('currentYear').textContent = year;
        
        // Get first day of month and number of days
        const firstDay = new Date(year, month, 1);
        const lastDay = new Date(year, month + 1, 0);
        const daysInMonth = lastDay.getDate();
        const startingDayOfWeek = firstDay.getDay();
        
        // Get previous month's last days
        const prevMonth = new Date(year, month, 0);
        const daysInPrevMonth = prevMonth.getDate();
        
        const calendarDays = document.getElementById('calendarDays');
        calendarDays.innerHTML = '';
        
        // Add previous month's trailing days
        for (let i = startingDayOfWeek - 1; i >= 0; i--) {
            const dayNum = daysInPrevMonth - i;
            const dayElement = this.createCalendarDay(dayNum, true, new Date(year, month - 1, dayNum));
            calendarDays.appendChild(dayElement);
        }
        
        // Add current month's days
        for (let day = 1; day <= daysInMonth; day++) {
            const currentDay = new Date(year, month, day);
            const dayElement = this.createCalendarDay(day, false, currentDay);
            calendarDays.appendChild(dayElement);
        }
        
        // Add next month's leading days
        const totalCells = calendarDays.children.length;
        const remainingCells = 42 - totalCells; // 6 rows × 7 days = 42 cells
        
        for (let day = 1; day <= remainingCells; day++) {
            const dayElement = this.createCalendarDay(day, true, new Date(year, month + 1, day));
            calendarDays.appendChild(dayElement);
        }
    }
    
    createCalendarDay(dayNum, isOtherMonth, date) {
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day';
        
        if (isOtherMonth) {
            dayElement.classList.add('other-month');
        }
        
        // Check if it's today
        const today = new Date();
        if (date.toDateString() === today.toDateString()) {
            dayElement.classList.add('today');
        }
        
        // Check if it's weekend
        const dayOfWeek = date.getDay();
        if (dayOfWeek === 0 || dayOfWeek === 6) {
            dayElement.classList.add('weekend');
        }
        
        // Check if it's a leave day
        const isLeaveDay = this.calendarData.leaveDays.some(leave => 
            leave.date.toDateString() === date.toDateString()
        );
        
        // Check if it's a late day
        const isLateDay = this.calendarData.lateDays.some(late => 
            late.date.toDateString() === date.toDateString()
        );
        
        if (isLeaveDay) {
            dayElement.classList.add('leave-day');
        }
        
        if (isLateDay) {
            dayElement.classList.add('late-day');
        }
        
        // Create day content
        const dayNumber = document.createElement('div');
        dayNumber.className = 'day-number';
        dayNumber.textContent = dayNum;
        dayElement.appendChild(dayNumber);
        
        // Add indicators
        const indicators = document.createElement('div');
        indicators.className = 'day-indicators';
        
        if (isLeaveDay) {
            const leaveIndicator = document.createElement('div');
            leaveIndicator.className = 'day-indicator indicator-leave';
            leaveIndicator.title = 'Leave Day';
            indicators.appendChild(leaveIndicator);
        }
        
        if (isLateDay) {
            const lateIndicator = document.createElement('div');
            lateIndicator.className = 'day-indicator indicator-late';
            lateIndicator.title = 'Late Day';
            indicators.appendChild(lateIndicator);
        }
        
        dayElement.appendChild(indicators);
        
        // Add click event for day details
        dayElement.addEventListener('click', () => {
            this.showDayDetails(date, isLeaveDay, isLateDay);
        });
        
        return dayElement;
    }
    
    showDayDetails(date, isLeaveDay, isLateDay) {
        if (!isLeaveDay && !isLateDay) return;
        
        let details = `📅 ${date.toLocaleDateString('en-US', { 
            weekday: 'long', 
            year: 'numeric', 
            month: 'long', 
            day: 'numeric' 
        })}\n\n`;
        
        if (isLeaveDay) {
            const leaveInfo = this.calendarData.leaveDays.find(leave => 
                leave.date.toDateString() === date.toDateString()
            );
            details += `🏖️ Leave Day\nType: ${leaveInfo.type}\nReason: ${leaveInfo.reason}\n\n`;
        }
        
        if (isLateDay) {
            const lateInfo = this.calendarData.lateDays.find(late => 
                late.date.toDateString() === date.toDateString()
            );
            details += `⏰ Late Attendance Record\n`;
            details += `Reason: ${lateInfo.reason || 'Not specified'}\n`;
            if (lateInfo.notes) {
                details += `Notes: ${lateInfo.notes}\n`;
            }
            details += `Marked by: ${lateInfo.markedBy || 'HR'}\n`;
            details += `Date: ${lateInfo.date.toLocaleDateString()}`;
        }
        
        alert(details);
    }
    
    navigateMonth(direction) {
        this.currentDate.setMonth(this.currentDate.getMonth() + direction);
        this.renderCalendar();
        this.updateCalendarSummary();
    }
    
    updateCalendarSummary() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        
        // Count leave days in current month
        const monthLeaveDays = this.calendarData.leaveDays.filter(leave => 
            leave.date.getFullYear() === year && leave.date.getMonth() === month
        ).length;
        
        // Count late days in current month
        const monthLateDays = this.calendarData.lateDays.filter(late => 
            late.date.getFullYear() === year && late.date.getMonth() === month
        ).length;
        
        // Calculate working days (excluding weekends)
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        let workingDays = 0;
        
        for (let day = 1; day <= daysInMonth; day++) {
            const date = new Date(year, month, day);
            const dayOfWeek = date.getDay();
            if (dayOfWeek !== 0 && dayOfWeek !== 6) { // Not Sunday or Saturday
                workingDays++;
            }
        }
        
        // Update summary display
        document.getElementById('monthLeaveDays').textContent = monthLeaveDays;
        document.getElementById('monthLateDays').textContent = monthLateDays;
        document.getElementById('monthWorkingDays').textContent = workingDays;
    }
    
    // ========== LATE ATTENDANCE METHODS ==========
    
    async loadLateAttendanceData() {
        try {
            console.log('⏰ Loading late attendance data...');
            
            // Load employee list for dropdown
            await this.loadEmployeeList();
            
            // Load recent late records
            await this.loadLateRecords();
            
            // Set default date to today
            const today = new Date().toISOString().split('T')[0];
            document.getElementById('lateDate').value = today;
            
        } catch (error) {
            console.error('Error loading late attendance data:', error);
        }
    }

    async loadLateAttendanceChart() {
        try {
            console.log('📊 Loading late attendance chart...');
            
            // Get current month's late attendance data
            const currentDate = new Date();
            const startDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).toISOString().split('T')[0];
            const endDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).toISOString().split('T')[0];
            
            const response = await fetch(`${this.baseURL}/api/late-attendance/range?startDate=${startDate}&endDate=${endDate}`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const result = await response.json();
            const lateRecords = result.data || [];
            
            // Group by employee and count late days
            const employeeLateCounts = {};
            lateRecords.forEach(record => {
                if (employeeLateCounts[record.employeeName]) {
                    employeeLateCounts[record.employeeName]++;
                } else {
                    employeeLateCounts[record.employeeName] = 1;
                }
            });
            
            // Create chart data
            const labels = Object.keys(employeeLateCounts);
            const data = Object.values(employeeLateCounts);
            
            // Update chart summary
            const totalLateDays = data.reduce((sum, count) => sum + count, 0);
            const employeesAffected = labels.length;
            
            document.getElementById('chartTotalLateDays').textContent = totalLateDays;
            document.getElementById('chartLateEmployees').textContent = employeesAffected;
            
            // Create or update chart
            const ctx = document.getElementById('lateAttendanceChart');
            if (ctx) {
                if (this.lateAttendanceChart) {
                    this.lateAttendanceChart.destroy();
                }
                
                this.lateAttendanceChart = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: 'Late Days This Month',
                            data: data,
                            backgroundColor: '#DC2626',
                            borderColor: '#B91C1C',
                            borderWidth: 1
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                            y: {
                                beginAtZero: true,
                                ticks: {
                                    stepSize: 1
                                }
                            }
                        },
                        plugins: {
                            legend: {
                                display: false
                            }
                        }
                    }
                });
            }
            
        } catch (error) {
            console.error('Error loading late attendance chart:', error);
            // Show empty state
            document.getElementById('chartTotalLateDays').textContent = '0';
            document.getElementById('chartLateEmployees').textContent = '0';
        }
    }

    showPendingLeaves() {
        console.log('📋 Showing pending leaves...');
        
        // Scroll to the HR Management section
        const hrManagementSection = document.querySelector('.dashboard-sub-section h3');
        if (hrManagementSection) {
            hrManagementSection.scrollIntoView({ behavior: 'smooth' });
        }
        
        // Show notification
        this.showNotification('Showing pending leave requests', 'info');
    }

    showTeamOnLeave() {
        console.log('👥 Showing team on leave...');
        
        // Scroll to the Team on Leave section in the main dashboard
        const teamOnLeaveSection = document.querySelector('#teamOnLeaveSection');
        if (teamOnLeaveSection) {
            teamOnLeaveSection.scrollIntoView({ behavior: 'smooth' });
        }
        
        // Show notification
        this.showNotification('Showing team members currently on leave', 'info');
    }

    async loadHRManagementData() {
        console.log('👨‍💼 Loading HR Management data...');
        
        try {
            await Promise.all([
                this.loadHRStats(),
                this.loadPendingRequests(),
                this.loadAllRequests(),
                this.loadLateAttendanceData()
            ]);
            
            this.showNotification('HR Management data loaded successfully', 'success');
        } catch (error) {
            console.error('Error loading HR Management data:', error);
            this.showNotification('Error loading HR Management data', 'error');
        }
    }

    async loadHRHistoryData() {
        try {
            console.log('📋 Loading HR history data...');
            

            
            const response = await fetch(`${this.baseURL}/api/dashboard/hr/all-requests`);
            const allRequests = await response.json();
            
            const tbody = document.getElementById('hrHistoryTableBody');
            if (!tbody) return;
            
            tbody.innerHTML = '';
            
            if (allRequests.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 20px; color: #64748b;">
                            <div style="display: flex; flex-direction: column; align-items: center; gap: 10px;">
                                <span style="font-size: 24px;">📋</span>
                                <span>No leave requests found</span>
                                <span style="font-size: 14px;">No leave history available</span>
                            </div>
                        </td>
                    </tr>
                `;
                return;
            }
            
            allRequests.forEach(leave => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${leave.employeeName}</td>
                    <td>${this.formatDate(leave.startDate)}</td>
                    <td>${this.formatDate(leave.endDate)}</td>
                    <td>${this.calculateDuration(leave.startDate, leave.endDate)} days</td>
                    <td>${leave.leaveType}</td>
                    <td>
                        <span class="status-badge status-${leave.status.toLowerCase()}">${leave.status}</span>
                        ${leave.rejectionReason ? `
                            <br><small style="color: #dc2626; font-size: 11px; margin-top: 4px; display: block;">
                                ❌ ${leave.rejectionReason}
                            </small>
                        ` : ''}
                    </td>
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
            console.error('Error loading HR history data:', error);
            // Show empty state
            const tbody = document.getElementById('hrHistoryTableBody');
            if (tbody) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 20px; color: #64748b;">
                            <div style="display: flex; flex-direction: column; align-items: center; gap: 10px;">
                                <span style="font-size: 24px;">❌</span>
                                <span>Error loading history</span>
                                <span style="font-size: 14px;">Please try again later</span>
                            </div>
                        </td>
                    </tr>
                `;
            }
        }
    }


    
    async loadEmployeeList() {
        try {
            // Get real employee list from backend
            const response = await fetch(`${this.baseURL}/api/employees`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const result = await response.json();
            const employees = result.data || [];
            
            const select = document.getElementById('lateEmployeeName');
            if (!select) return;
            
            select.innerHTML = '<option value="">Select Employee</option>';
            employees.forEach(employee => {
                const option = document.createElement('option');
                option.value = employee.fullName;
                option.textContent = employee.fullName;
                select.appendChild(option);
            });
            
        } catch (error) {
            console.error('Error loading employee list:', error);
            // Fallback to empty list
            const select = document.getElementById('lateEmployeeName');
            if (select) {
                select.innerHTML = '<option value="">No employees available</option>';
            }
        }
    }
    
    async loadLateRecords() {
        try {
            // Get real late records from backend
            const response = await fetch(`${this.baseURL}/api/late-attendance/range?startDate=2024-01-01&endDate=2024-12-31`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const result = await response.json();
            const lateRecords = result.data || [];
            
            this.renderLateRecords(lateRecords);
            
        } catch (error) {
            console.error('Error loading late records:', error);
            // Show empty state
            this.renderLateRecords([]);
        }
    }
    
    renderLateRecords(records) {
        const container = document.getElementById('lateRecordsList');
        if (!container) return;
        
        container.innerHTML = '';
        
        if (records.length === 0) {
            container.innerHTML = `
                <div class="no-data" style="text-align: center; padding: 20px; color: #64748b;">
                    <div style="font-size: 24px; margin-bottom: 10px;">✅</div>
                    <div>No late attendance records</div>
                    <div style="font-size: 14px; margin-top: 5px;">All employees arrived on time</div>
                </div>
            `;
            return;
        }
        
        records.forEach(record => {
            const recordCard = document.createElement('div');
            recordCard.className = 'late-record-card';
            
            recordCard.innerHTML = `
                <div class="late-record-header">
                    <div class="late-record-employee">${record.employeeName}</div>
                    <div class="late-record-date">${this.formatDate(record.date)}</div>
                </div>
                <div class="late-record-details">
                    <div class="late-record-detail">
                        <div class="late-record-detail-label">Reason</div>
                        <div class="late-record-detail-value">${record.reason}</div>
                    </div>
                    <div class="late-record-detail">
                        <div class="late-record-detail-label">Marked By</div>
                        <div class="late-record-detail-value">${record.markedBy}</div>
                    </div>
                </div>
                ${record.notes ? `
                    <div class="late-record-notes">
                        <div class="late-record-notes-label">Notes</div>
                        <div class="late-record-notes-text">${record.notes}</div>
                    </div>
                ` : ''}
            `;
            
            container.appendChild(recordCard);
        });
    }
    
    async markEmployeeLate() {
        try {
            const employeeName = document.getElementById('lateEmployeeName').value;
            const date = document.getElementById('lateDate').value;
            const reason = document.getElementById('lateReason').value;
            const notes = document.getElementById('lateNotes').value;
            
            if (!employeeName || !date || !reason) {
                this.showNotification('Please fill in all required fields', 'error');
                return;
            }
            
            // Send request to backend
            const response = await fetch(`${this.baseURL}/api/late-attendance/mark-late`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    employeeName: employeeName,
                    date: date,
                    reason: reason,
                    notes: notes
                })
            });
            
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to mark employee as late');
            }
            
            const result = await response.json();
            this.showNotification(result.message || `Successfully marked ${employeeName} as late on ${this.formatDate(date)}`, 'success');
            
            // Clear form
            document.getElementById('lateEmployeeName').value = '';
            document.getElementById('lateDate').value = new Date().toISOString().split('T')[0];
            document.getElementById('lateReason').value = '';
            document.getElementById('lateNotes').value = '';
            
            // Reload late records
            await this.loadLateRecords();
            
        } catch (error) {
            console.error('Error marking employee as late:', error);
            this.showNotification(error.message || 'Error marking employee as late', 'error');
        }
    }
    
    async filterLateRecords() {
        const dateFilter = document.getElementById('lateDateFilter').value;
        console.log('Filtering late records by date:', dateFilter);
        
        if (!dateFilter) {
            // If no date filter, load all records
            await this.loadLateRecords();
            return;
        }
        
        try {
            // Get late records for specific date
            const response = await fetch(`${this.baseURL}/api/late-attendance/date/${dateFilter}`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const result = await response.json();
            const lateRecords = result.data || [];
            
            this.renderLateRecords(lateRecords);
            
        } catch (error) {
            console.error('Error filtering late records:', error);
            this.showNotification('Error filtering late records', 'error');
        }
    }
    
    // Update calendar to show late days for employees
    async loadUserAttendanceData() {
        try {
            // Get real late attendance data from backend
            const response = await fetch(`${this.baseURL}/api/late-attendance/my-late-records`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            const result = await response.json();
            const lateRecords = result.data || [];
            
            // Convert to calendar format
            this.calendarData.lateDays = lateRecords.map(record => ({
                date: new Date(record.date),
                reason: record.reason
            }));
            
            console.log('✅ Loaded late days:', this.calendarData.lateDays.length);
            
        } catch (error) {
            console.error('Error loading attendance data:', error);
            this.calendarData.lateDays = [];
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

// HR Rejection Modal Functions
function closeHRRejectionModal() {
    const modal = document.getElementById('hrRejectionModal');
    modal.style.display = 'none';
    // Clear the current rejection leave ID
    if (window.dashboard) {
        window.dashboard.currentRejectionLeaveId = null;
    }
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