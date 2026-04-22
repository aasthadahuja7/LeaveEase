import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './Signup.css';

const SignupDebug = () => {
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('Testing signup...');

    try {
      const response = await axios.post('http://localhost:8000/auth/signup', {
        username: 'testuser',
        email: 'test@example.com',
        password: 'password123',
        full_name: 'Test User',
        department: 'Engineering',
        role: 'EMPLOYEE'
      });
      
      console.log('Signup response:', response);
      setMessage('Signup successful!');
      
      setTimeout(() => {
        navigate('/login');
      }, 2000);
      
    } catch (error) {
      console.error('Signup error:', error);
      let errorText = 'Signup failed';
      
      if (error.response?.data?.detail) {
        errorText = error.response.data.detail;
      } else if (error.response?.data?.message) {
        errorText = error.response.data.message;
      } else if (error.message) {
        errorText = error.message;
      }
      
      setMessage(errorText);
    }
  };

  return (
    <div className="signup-container">
      <div className="signup-card">
        <h2>Debug Signup</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Test User Creation</label>
            <button type="submit">Create Test User</button>
          </div>

          {message && (
            <div className={`message ${message.includes('success') ? 'success' : 'error'}`}>
              {message}
            </div>
          )}
        </form>

        <div className="login-link">
          <p><a href="/login">Back to Login</a></p>
        </div>
      </div>
    </div>
  );
};

export default SignupDebug;
