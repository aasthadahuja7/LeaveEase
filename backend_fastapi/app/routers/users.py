from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List

from app.database import get_db
from app.models import User, Role
from app.schemas import UserResponse, UserUpdate
from app.auth import get_current_active_user, get_hr_user, create_user_response, get_password_hash

router = APIRouter()

@router.get("/", response_model=List[UserResponse])
async def get_users(
    current_user: User = Depends(get_hr_user),
    db: Session = Depends(get_db),
    skip: int = 0,
    limit: int = 100
):
    users = db.query(User).offset(skip).limit(limit).all()
    return [create_user_response(user) for user in users]

@router.get("/{user_id}", response_model=UserResponse)
async def get_user(
    user_id: int,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    # Users can only see their own profile unless they're HR/Admin
    if (current_user.role not in [Role.HR, Role.ADMIN] and 
        current_user.id != user_id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
    
    return create_user_response(user)

@router.put("/{user_id}", response_model=UserResponse)
async def update_user(
    user_id: int,
    user_update: UserUpdate,
    current_user: User = Depends(get_current_active_user),
    db: Session = Depends(get_db)
):
    # Users can only update their own profile unless they're HR/Admin
    if (current_user.role not in [Role.HR, Role.ADMIN] and 
        current_user.id != user_id):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
    
    # Update fields if provided
    if user_update.full_name is not None:
        user.full_name = user_update.full_name
    if user_update.department is not None:
        user.department = user_update.department
    if user_update.employee_code is not None:
        user.employee_code = user_update.employee_code
    if user_update.profile_picture is not None:
        user.profile_picture = user_update.profile_picture
    
    db.commit()
    db.refresh(user)
    
    return create_user_response(user)

@router.delete("/{user_id}")
async def delete_user(
    user_id: int,
    current_user: User = Depends(get_hr_user),
    db: Session = Depends(get_db)
):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )
    
    # Don't allow users to delete themselves
    if user.id == current_user.id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Cannot delete your own account"
        )
    
    db.delete(user)
    db.commit()
    
    return {"message": "User deleted successfully"}

@router.get("/role/{role}", response_model=List[UserResponse])
async def get_users_by_role(
    role: Role,
    current_user: User = Depends(get_hr_user),
    db: Session = Depends(get_db)
):
    users = db.query(User).filter(User.role == role).all()
    return [create_user_response(user) for user in users]
