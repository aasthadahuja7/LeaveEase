from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from datetime import date

from app.database import get_db
from app.models import User, LeaveRequest, Role, LeaveStatus
from app.schemas import (
    LeaveRequestCreate, LeaveRequestResponse, LeaveRequestUpdate
)
from app.auth import get_current_active_user, get_hr_user
from app.auth import get_current_active_user as get_current_user_auth

router = APIRouter()

@router.post("/", response_model=LeaveRequestResponse)
async def create_leave_request(
    leave_data: LeaveRequestCreate,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    # Create leave request
    db_leave = LeaveRequest(
        employee_id=current_user.id,
        employee_name=current_user.full_name,
        start_date=leave_data.start_date,
        end_date=leave_data.end_date,
        reason=leave_data.reason,
        leave_type=leave_data.leave_type,
        status=LeaveStatus.PENDING
    )
    
    db.add(db_leave)
    db.commit()
    db.refresh(db_leave)
    
    return db_leave

@router.get("/", response_model=List[LeaveRequestResponse])
async def get_leave_requests(
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db),
    skip: int = 0,
    limit: int = 100
):
    if current_user.role in [Role.HR, Role.ADMIN]:
        # HR can see all leave requests
        leaves = db.query(LeaveRequest).offset(skip).limit(limit).all()
    else:
        # Employees can only see their own leave requests
        leaves = db.query(LeaveRequest).filter(
            LeaveRequest.employee_id == current_user.id
        ).offset(skip).limit(limit).all()
    
    return leaves

@router.get("/{leave_id}", response_model=LeaveRequestResponse)
async def get_leave_request(
    leave_id: int,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    leave = db.query(LeaveRequest).filter(LeaveRequest.id == leave_id).first()
    if not leave:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Leave request not found"
        )
    
    # Check permissions
    if (current_user.role not in [Role.HR, Role.ADMIN] and 
        leave.employee_id != current_user.id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    return leave

@router.put("/{leave_id}/approve", response_model=LeaveRequestResponse)
async def approve_leave_request(
    leave_id: int,
    current_user: User = Depends(get_hr_user),
    db: Session = Depends(get_db)
):
    leave = db.query(LeaveRequest).filter(LeaveRequest.id == leave_id).first()
    if not leave:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Leave request not found"
        )
    
    if leave.status != LeaveStatus.PENDING:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Leave request is not pending"
        )
    
    leave.status = LeaveStatus.APPROVED
    leave.approved_by = current_user.id
    db.commit()
    db.refresh(leave)
    
    return leave

@router.put("/{leave_id}/reject", response_model=LeaveRequestResponse)
async def reject_leave_request(
    leave_id: int,
    rejection_data: LeaveRequestUpdate,
    current_user: User = Depends(get_hr_user),
    db: Session = Depends(get_db)
):
    leave = db.query(LeaveRequest).filter(LeaveRequest.id == leave_id).first()
    if not leave:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Leave request not found"
        )
    
    if leave.status != LeaveStatus.PENDING:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Leave request is not pending"
        )
    
    leave.status = LeaveStatus.REJECTED
    leave.rejection_reason = rejection_data.rejection_reason
    leave.approved_by = current_user.id
    db.commit()
    db.refresh(leave)
    
    return leave

@router.get("/status/{status}", response_model=List[LeaveRequestResponse])
async def get_leaves_by_status(
    status: LeaveStatus,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    if current_user.role in [Role.HR, Role.ADMIN]:
        leaves = db.query(LeaveRequest).filter(LeaveRequest.status == status).all()
    else:
        leaves = db.query(LeaveRequest).filter(
            LeaveRequest.employee_id == current_user.id,
            LeaveRequest.status == status
        ).all()
    
    return leaves
