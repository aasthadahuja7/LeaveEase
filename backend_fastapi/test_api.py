"""
Test script for LeaveEase FastAPI API endpoints
"""

import requests
import json
from datetime import date, timedelta

BASE_URL = "http://localhost:8000"

def test_authentication():
    """Test authentication endpoints"""
    print("Testing Authentication...")
    
    # Test login
    login_data = {
        "username": "hr_user",
        "password": "password123"
    }
    
    response = requests.post(f"{BASE_URL}/auth/login", json=login_data)
    print(f"Login Status: {response.status_code}")
    
    if response.status_code == 200:
        token_data = response.json()
        print(f"Login successful! Token: {token_data['access_token'][:20]}...")
        return token_data['access_token']
    else:
        print(f"Login failed: {response.text}")
        return None

def test_current_user(token):
    """Test getting current user info"""
    print("\nTesting Current User...")
    
    headers = {"Authorization": f"Bearer {token}"}
    response = requests.get(f"{BASE_URL}/auth/me", headers=headers)
    print(f"Current User Status: {response.status_code}")
    
    if response.status_code == 200:
        user_data = response.json()
        print(f"User: {user_data['full_name']} ({user_data['role']})")
    else:
        print(f"Failed to get current user: {response.text}")

def test_leave_requests(token):
    """Test leave request endpoints"""
    print("\nTesting Leave Requests...")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    # Test creating a leave request
    leave_data = {
        "start_date": (date.today() + timedelta(days=5)).isoformat(),
        "end_date": (date.today() + timedelta(days=7)).isoformat(),
        "reason": "Test vacation",
        "leave_type": "ANNUAL"
    }
    
    response = requests.post(f"{BASE_URL}/leaves/", json=leave_data, headers=headers)
    print(f"Create Leave Request Status: {response.status_code}")
    
    if response.status_code == 200:
        leave = response.json()
        print(f"Leave request created: ID {leave['id']}")
        leave_id = leave['id']
        
        # Test getting leave requests
        response = requests.get(f"{BASE_URL}/leaves/", headers=headers)
        print(f"Get Leave Requests Status: {response.status_code}")
        
        if response.status_code == 200:
            leaves = response.json()
            print(f"Found {len(leaves)} leave requests")
            
            # Test approving leave (if HR)
            if leaves and len(leaves) > 0:
                leave_to_approve = leaves[0]
                if leave_to_approve['status'] == 'PENDING':
                    response = requests.put(
                        f"{BASE_URL}/leaves/{leave_to_approve['id']}/approve",
                        headers=headers
                    )
                    print(f"Approve Leave Status: {response.status_code}")
    else:
        print(f"Failed to create leave request: {response.text}")

def test_dashboard(token):
    """Test dashboard endpoints"""
    print("\nTesting Dashboard...")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    # Test HR dashboard
    response = requests.get(f"{BASE_URL}/dashboard/hr", headers=headers)
    print(f"HR Dashboard Status: {response.status_code}")
    
    if response.status_code == 200:
        dashboard_data = response.json()
        print(f"Dashboard Stats: {dashboard_data.get('stats', {})}")
    else:
        print(f"Failed to get HR dashboard: {response.text}")

def test_users(token):
    """Test user management endpoints"""
    print("\nTesting User Management...")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    # Test getting users
    response = requests.get(f"{BASE_URL}/users/", headers=headers)
    print(f"Get Users Status: {response.status_code}")
    
    if response.status_code == 200:
        users = response.json()
        print(f"Found {len(users)} users")
        for user in users[:3]:  # Show first 3 users
            print(f"  - {user['full_name']} ({user['role']})")
    else:
        print(f"Failed to get users: {response.text}")

def test_health():
    """Test health endpoint"""
    print("\nTesting Health Check...")
    
    response = requests.get(f"{BASE_URL}/health")
    print(f"Health Check Status: {response.status_code}")
    
    if response.status_code == 200:
        health_data = response.json()
        print(f"Health: {health_data.get('status')}")

def run_all_tests():
    """Run all API tests"""
    print("=== LeaveEase API Test Suite ===\n")
    
    # Test health endpoint first
    test_health()
    
    # Test authentication
    token = test_authentication()
    
    if token:
        # Test authenticated endpoints
        test_current_user(token)
        test_leave_requests(token)
        test_dashboard(token)
        test_users(token)
    else:
        print("Authentication failed. Skipping other tests.")
    
    print("\n=== Test Suite Complete ===")

if __name__ == "__main__":
    run_all_tests()
