import React, { useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import './LeaveRequest.css';

const LeaveRequest = () => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    start_date: '',
    end_date: '',
    reason: '',
    leave_type: 'ANNUAL'
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');

    try {
      await axios.post('http://localhost:8000/leaves/', formData);
      setMessage('Leave request submitted successfully!');
      setFormData({
        start_date: '',
        end_date: '',
        reason: '',
        leave_type: 'ANNUAL'
      });
    } catch (error) {
      setMessage('Failed to submit leave request. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="leave-request-container">
      <div className="leave-request-card">
        <h2>Submit Leave Request</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="start_date">Start Date</label>
              <input
                type="date"
                id="start_date"
                name="start_date"
                value={formData.start_date}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="end_date">End Date</label>
              <input
                type="date"
                id="end_date"
                name="end_date"
                value={formData.end_date}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="leave_type">Leave Type</label>
            <select
              id="leave_type"
              name="leave_type"
              value={formData.leave_type}
              onChange={handleChange}
            >
              <option value="ANNUAL">Annual Leave</option>
              <option value="SICK">Sick Leave</option>
              <option value="PERSONAL">Personal Leave</option>
              <option value="MATERNITY">Maternity Leave</option>
              <option value="PATERNITY">Paternity Leave</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="reason">Reason</label>
            <textarea
              id="reason"
              name="reason"
              value={formData.reason}
              onChange={handleChange}
              rows="4"
              placeholder="Please provide a reason for your leave request..."
            />
          </div>

          {message && (
            <div className={`message ${message.includes('success') ? 'success' : 'error'}`}>
              {message}
            </div>
          )}

          <button type="submit" disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Leave Request'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default LeaveRequest;
