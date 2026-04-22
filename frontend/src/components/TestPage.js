import React from 'react';

const TestPage = () => {
  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>LeaveEase Test Page</h1>
      <p>If you can see this, React is working!</p>
      <button onClick={() => alert('Button clicked!')}>Test Button</button>
    </div>
  );
};

export default TestPage;
