import React from 'react';
import './Dashboard.css';

const DashboardTest = () => {
  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Test Dashboard</h1>
        <div className="user-info">
          <span>Test User</span>
          <span className="role-badge">HR</span>
        </div>
      </div>
      
      <div style={{ padding: '2rem', background: 'white', margin: '1rem', borderRadius: '8px' }}>
        <h2>Dashboard Content</h2>
        <p>If you can see this styled content, CSS is working!</p>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem', marginTop: '2rem' }}>
          <div style={{ background: '#e3f2fd', padding: '1rem', borderRadius: '8px', textAlign: 'center' }}>
            <h3>Total Employees</h3>
            <p style={{ fontSize: '2rem', margin: '0' }}>4</p>
          </div>
          <div style={{ background: '#f0f9ff', padding: '1rem', borderRadius: '8px', textAlign: 'center' }}>
            <h3>Pending Leaves</h3>
            <p style={{ fontSize: '2rem', margin: '0' }}>2</p>
          </div>
          <div style={{ background: '#d4edda', padding: '1rem', borderRadius: '8px', textAlign: 'center' }}>
            <h3>Approved Leaves</h3>
            <p style={{ fontSize: '2rem', margin: '0' }}>8</p>
          </div>
          <div style={{ background: '#f8d7da', padding: '1rem', borderRadius: '8px', textAlign: 'center' }}>
            <h3>Rejected Leaves</h3>
            <p style={{ fontSize: '2rem', margin: '0' }}>1</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardTest;
