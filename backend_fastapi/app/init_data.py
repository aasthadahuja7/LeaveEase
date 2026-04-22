"""
Data initialization script for LeaveEase FastAPI backend
This script creates demo users and sample data for testing
"""

import sys
import os
import asyncio
from datetime import date, datetime, timedelta
from sqlalchemy.orm import Session

# Add the current directory to Python path
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.database import SessionLocal, engine
from app.models import Base, User, LeaveRequest, LateAttendance, Role, LeaveStatus, LeaveType
from app.auth import get_password_hash

def create_demo_users(db: Session):
    """Create demo users for testing"""
    print("Creating demo users...")
    
    demo_users = [
        {
            "username": "hr_user",
            "email": "hr@company.com",
            "password": "password123",
            "full_name": "HR Manager",
            "department": "Human Resources",
            "employee_code": "HR001",
            "role": Role.HR
        },
        {
            "username": "john_doe",
            "email": "john.doe@company.com",
            "password": "password123",
            "full_name": "John Doe",
            "department": "Engineering",
            "employee_code": "ENG001",
            "role": Role.EMPLOYEE
        },
        {
            "username": "jane_smith",
            "email": "jane.smith@company.com",
            "password": "password123",
            "full_name": "Jane Smith",
            "department": "Engineering",
            "employee_code": "ENG002",
            "role": Role.EMPLOYEE
        },
        {
            "username": "mike_johnson",
            "email": "mike.johnson@company.com",
            "password": "password123",
            "full_name": "Mike Johnson",
            "department": "Marketing",
            "employee_code": "MKT001",
            "role": Role.EMPLOYEE
        }
    ]
    
    for user_data in demo_users:
        # Check if user already exists
        existing_user = db.query(User).filter(User.username == user_data["username"]).first()
        if not existing_user:
            user = User(
                username=user_data["username"],
                email=user_data["email"],
                password_hash=get_password_hash(user_data["password"][:72]),
                full_name=user_data["full_name"],
                department=user_data["department"],
                employee_code=user_data["employee_code"],
                role=user_data["role"]
            )
            db.add(user)
            print(f"Created user: {user_data['username']}")
        else:
            print(f"User already exists: {user_data['username']}")
    
    db.commit()
    print("Demo users created successfully!")

def create_sample_leave_requests(db: Session):
    """Create sample leave requests"""
    print("Creating sample leave requests...")
    
    # Get users
    john = db.query(User).filter(User.username == "john_doe").first()
    jane = db.query(User).filter(User.username == "jane_smith").first()
    mike = db.query(User).filter(User.username == "mike_johnson").first()
    
    if not all([john, jane, mike]):
        print("Users not found. Please create demo users first.")
        return
    
    sample_leaves = [
        {
            "employee": john,
            "start_date": date.today() + timedelta(days=10),
            "end_date": date.today() + timedelta(days=15),
            "reason": "Family vacation",
            "leave_type": LeaveType.ANNUAL,
            "status": LeaveStatus.PENDING
        },
        {
            "employee": jane,
            "start_date": date.today() + timedelta(days=20),
            "end_date": date.today() + timedelta(days=22),
            "reason": "Medical appointment",
            "leave_type": LeaveType.SICK,
            "status": LeaveStatus.APPROVED
        },
        {
            "employee": mike,
            "start_date": date.today() + timedelta(days=5),
            "end_date": date.today() + timedelta(days=9),
            "reason": "Business trip",
            "leave_type": LeaveType.ANNUAL,
            "status": LeaveStatus.PENDING
        },
        {
            "employee": john,
            "start_date": date.today() - timedelta(days=10),
            "end_date": date.today() - timedelta(days=8),
            "reason": "Personal matters",
            "leave_type": LeaveType.PERSONAL,
            "status": LeaveStatus.REJECTED,
            "rejection_reason": "Insufficient notice period"
        }
    ]
    
    for leave_data in sample_leaves:
        # Check if similar leave request already exists
        existing = db.query(LeaveRequest).filter(
            LeaveRequest.employee_id == leave_data["employee"].id,
            LeaveRequest.start_date == leave_data["start_date"]
        ).first()
        
        if not existing:
            leave = LeaveRequest(
                employee_id=leave_data["employee"].id,
                employee_name=leave_data["employee"].full_name,
                start_date=leave_data["start_date"],
                end_date=leave_data["end_date"],
                reason=leave_data["reason"],
                leave_type=leave_data["leave_type"],
                status=leave_data["status"],
                rejection_reason=leave_data.get("rejection_reason")
            )
            db.add(leave)
            print(f"Created leave request for {leave_data['employee'].full_name}")
        else:
            print(f"Leave request already exists for {leave_data['employee'].full_name}")
    
    db.commit()
    print("Sample leave requests created successfully!")

def create_sample_late_attendances(db: Session):
    """Create sample late attendance records"""
    print("Creating sample late attendances...")
    
    # Get users
    john = db.query(User).filter(User.username == "john_doe").first()
    jane = db.query(User).filter(User.username == "jane_smith").first()
    hr_user = db.query(User).filter(User.username == "hr_user").first()
    
    if not all([john, jane, hr_user]):
        print("Users not found. Please create demo users first.")
        return
    
    sample_late_attendances = [
        {
            "employee": john,
            "date": date.today() - timedelta(days=2),
            "late_reason": "Traffic jam on highway"
        },
        {
            "employee": jane,
            "date": date.today() - timedelta(days=5),
            "late_reason": "Car broke down"
        },
        {
            "employee": john,
            "date": date.today() - timedelta(days=7),
            "late_reason": "Medical emergency"
        }
    ]
    
    for attendance_data in sample_late_attendances:
        # Check if similar record already exists
        existing = db.query(LateAttendance).filter(
            LateAttendance.employee_id == attendance_data["employee"].id,
            LateAttendance.date == attendance_data["date"]
        ).first()
        
        if not existing:
            attendance = LateAttendance(
                employee_id=attendance_data["employee"].id,
                employee_name=attendance_data["employee"].full_name,
                date=attendance_data["date"],
                late_reason=attendance_data["late_reason"],
                approved_by=hr_user.id if hr_user else None
            )
            db.add(attendance)
            print(f"Created late attendance for {attendance_data['employee'].full_name}")
        else:
            print(f"Late attendance already exists for {attendance_data['employee'].full_name}")
    
    db.commit()
    print("Sample late attendances created successfully!")

def init_database():
    """Initialize the database with demo data"""
    print("Starting database initialization...")
    
    # Create all tables
    Base.metadata.create_all(bind=engine)
    print("Database tables created.")
    
    # Create session
    db = SessionLocal()
    
    try:
        # Create demo data
        create_demo_users(db)
        create_sample_leave_requests(db)
        create_sample_late_attendances(db)
        
        print("\nDatabase initialization completed successfully!")
        print("\nDemo accounts:")
        print("- HR User: hr_user / password123")
        print("- Employee: john_doe / password123")
        print("- Employee: jane_smith / password123")
        print("- Employee: mike_johnson / password123")
        
    except Exception as e:
        print(f"Error during initialization: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    init_database()
