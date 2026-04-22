import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Signup from './components/Signup';
import Dashboard from './components/Dashboard';
import DashboardTest from './components/DashboardTest';
import LeaveRequest from './components/LeaveRequest';
import TestPage from './components/TestPage';
import { AuthProvider, useAuth } from './contexts/AuthContext';

function ProtectedRoute({ children }) {
  const { user } = useAuth();
  return user ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />
            <Route path="/test" element={<TestPage />} />
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } />
            <Route path="/dashboard-test" element={
              <ProtectedRoute>
                <DashboardTest />
              </ProtectedRoute>
            } />
            <Route path="/leave-request" element={
              <ProtectedRoute>
                <LeaveRequest />
              </ProtectedRoute>
            } />
            <Route path="/" element={<Navigate to="/dashboard" />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
