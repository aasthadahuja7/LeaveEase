from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy import func, and_
from typing import List

from app.database import get_db
from app.models import User, LeaveRequest, LateAttendance, Role, LeaveStatus
from app.schemas import DashboardStats, EmployeeDashboard, HRDashboard
from app.auth import get_current_active_user, get_hr_user

router = APIRouter()

@router.get("/stats", response_model=DashboardStats)
async def get_dashboard_stats(
    current_user: User = Depends(get_hr_user),
    db: Session = Depends(get_db)
):
    total_employees = db.query(User).filter(User.role == Role.EMPLOYEE).count()
    pending_leaves = db.query(LeaveRequest).filter(LeaveRequest.status == LeaveStatus.PENDING).count()
    approved_leaves = db.query(LeaveRequest).filter(LeaveRequest.status == LeaveStatus.APPROVED).count()
    rejected_leaves = db.query(LeaveRequest).filter(LeaveRequest.status == LeaveStatus.REJECTED).count()
    late_attendances = db.query(LateAttendance).count()
    
    return DashboardStats(
        total_employees=total_employees,
        pending_leaves=pending_leaves,
        approved_leaves=approved_leaves,
        rejected_leaves=rejected_leaves,
        late_attendances=late_attendances
    )

@router.get("/employee", response_model=EmployeeDashboard)
async def get_employee_dashboard(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    # Get recent leave requests
    recent_leaves = db.query(LeaveRequest).filter(
        LeaveRequest.employee_id == current_user.id
    ).order_by(LeaveRequest.created_at.desc()).limit(10).all()
    
    # Get upcoming approved leaves
    upcoming_leaves = db.query(LeaveRequest).filter(
        and_(
            LeaveRequest.employee_id == current_user.id,
            LeaveRequest.status == LeaveStatus.APPROVED
        )
    ).order_by(LeaveRequest.start_date.asc()).limit(5).all()
    
    # Calculate leave balance (simplified - in real app you'd have more complex logic)
    total_annual_leaves = 21  # Example: 21 days per year
    used_leaves = db.query(func.sum(LeaveRequest.leave_duration)).filter(
        and_(
            LeaveRequest.employee_id == current_user.id,
            LeaveRequest.status == LeaveStatus.APPROVED
        )
    ).scalar() or 0
    
    leave_balance = {
        "annual": total_annual_leaves - used_leaves,
        "sick": 10,  # Example values
        "personal": 5
    }
    
    return EmployeeDashboard(
        user=current_user,
        recent_leaves=recent_leaves,
        leave_balance=leave_balance,
        upcoming_leaves=upcoming_leaves
    )

@router.get("/hr", response_model=HRDashboard)
async def get_hr_dashboard(
    current_user: User = Depends(get_hr_user),
    db: Session = Depends(get_db)
):
    # Get stats
    stats = await get_dashboard_stats(current_user, db)
    
    # Get pending leave requests
    pending_leaves = db.query(LeaveRequest).filter(
        LeaveRequest.status == LeaveStatus.PENDING
    ).order_by(LeaveRequest.created_at.desc()).limit(10).all()
    
    # Get recent leave requests
    recent_leaves = db.query(LeaveRequest).order_by(
        LeaveRequest.created_at.desc()
    ).limit(10).all()
    
    # Get recent late attendances
    late_attendances = db.query(LateAttendance).order_by(
        LateAttendance.created_at.desc()
    ).limit(10).all()
    
    return HRDashboard(
        stats=stats,
        pending_leaves=pending_leaves,
        recent_leaves=recent_leaves,
        late_attendances=late_attendances
    )

@router.get("/redirect")
async def dashboard_redirect(current_user: User = Depends(get_current_active_user)):
    if current_user.role in [Role.HR, Role.ADMIN]:
        return {"redirect": "/dashboard/hr"}
    else:
        return {"redirect": "/dashboard/employee"}
