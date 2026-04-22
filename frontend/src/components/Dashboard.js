import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import './Dashboard.css';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const endpoint = user?.role === 'HR' ? '/dashboard/hr' : '/dashboard/employee';
        const response = await axios.get(`http://localhost:8000${endpoint}`);
        setDashboardData(response.data);
      } catch (err) {
        setError('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchDashboardData();
    }
  }, [user]);

  if (loading) return <div className="loading">Loading dashboard...</div>;
  if (error) return <div className="error">{error}</div>;

  const isHR = user?.role === 'HR';

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <h1>LeaveEase Dashboard</h1>
        <div className="user-info">
          <span>Welcome, {user?.full_name}</span>
          <span className="role-badge">{user?.role}</span>
          <button onClick={logout} className="logout-btn">Logout</button>
        </div>
      </header>

      {isHR ? (
        <HRDashboard data={dashboardData} />
      ) : (
        <EmployeeDashboard data={dashboardData} />
      )}
    </div>
  );
};

const HRDashboard = ({ data }) => {
  return (
    <div className="hr-dashboard">
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Employees</h3>
          <p className="stat-number">{data?.stats?.total_employees || 0}</p>
        </div>
        <div className="stat-card">
          <h3>Pending Leaves</h3>
          <p className="stat-number">{data?.stats?.pending_leaves || 0}</p>
        </div>
        <div className="stat-card">
          <h3>Approved Leaves</h3>
          <p className="stat-number">{data?.stats?.approved_leaves || 0}</p>
        </div>
        <div className="stat-card">
          <h3>Late Attendances</h3>
          <p className="stat-number">{data?.stats?.late_attendances || 0}</p>
        </div>
      </div>

      <div className="dashboard-sections">
        <div className="section">
          <h2>Pending Leave Requests</h2>
          <div className="leave-list">
            {data?.pending_leaves?.map(leave => (
              <div key={leave.id} className="leave-item">
                <div className="leave-info">
                  <strong>{leave.employee_name}</strong>
                  <span>{leave.leave_type}</span>
                  <span>{leave.start_date} to {leave.end_date}</span>
                </div>
                <div className="leave-actions">
                  <button className="approve-btn">Approve</button>
                  <button className="reject-btn">Reject</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

const EmployeeDashboard = ({ data }) => {
  return (
    <div className="employee-dashboard">
      <div className="welcome-section">
        <h2>Welcome back, {data?.user?.full_name}!</h2>
        <p>Department: {data?.user?.department}</p>
      </div>

      <div className="leave-balance">
        <h3>Your Leave Balance</h3>
        <div className="balance-grid">
          <div className="balance-item">
            <span>Annual Leave</span>
            <span>{data?.leave_balance?.annual || 0} days</span>
          </div>
          <div className="balance-item">
            <span>Sick Leave</span>
            <span>{data?.leave_balance?.sick || 0} days</span>
          </div>
          <div className="balance-item">
            <span>Personal Leave</span>
            <span>{data?.leave_balance?.personal || 0} days</span>
          </div>
        </div>
      </div>

      <div className="recent-leaves">
        <h3>Recent Leave Requests</h3>
        <div className="leave-list">
          {data?.recent_leaves?.map(leave => (
            <div key={leave.id} className="leave-item">
              <div className="leave-info">
                <span className={`status-badge ${leave.status.toLowerCase()}`}>
                  {leave.status}
                </span>
                <span>{leave.leave_type}</span>
                <span>{leave.start_date} to {leave.end_date}</span>
                <span>{leave.leave_duration} days</span>
              </div>
              {leave.reason && <p className="leave-reason">{leave.reason}</p>}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
