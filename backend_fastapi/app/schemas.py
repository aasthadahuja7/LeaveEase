from pydantic import BaseModel, EmailStr, validator
from typing import Optional, List
from datetime import date, datetime
from enum import Enum

class Role(str, Enum):
    EMPLOYEE = "EMPLOYEE"
    HR = "HR"
    ADMIN = "ADMIN"

class LeaveStatus(str, Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"

class LeaveType(str, Enum):
    ANNUAL = "ANNUAL"
    SICK = "SICK"
    PERSONAL = "PERSONAL"
    MATERNITY = "MATERNITY"
    PATERNITY = "PATERNITY"

# User schemas
class UserBase(BaseModel):
    username: str
    email: EmailStr
    full_name: str
    department: Optional[str] = None
    employee_code: Optional[str] = None
    role: Role = Role.EMPLOYEE

class UserCreate(UserBase):
    password: str
    
    @validator('password')
    def validate_password(cls, v):
        if len(v) < 6:
            raise ValueError('Password must be at least 6 characters long')
        return v

class UserUpdate(BaseModel):
    full_name: Optional[str] = None
    department: Optional[str] = None
    employee_code: Optional[str] = None
    profile_picture: Optional[str] = None

class UserResponse(UserBase):
    id: int
    is_active: bool
    created_at: datetime
    
    class Config:
        from_attributes = True

# Auth schemas
class LoginRequest(BaseModel):
    username: str
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str
    user: UserResponse

class TokenData(BaseModel):
    username: Optional[str] = None

# Leave Request schemas
class LeaveRequestBase(BaseModel):
    start_date: date
    end_date: date
    reason: Optional[str] = None
    leave_type: LeaveType = LeaveType.ANNUAL
    
    @validator('end_date')
    def validate_end_date(cls, v, values):
        if 'start_date' in values and v < values['start_date']:
            raise ValueError('End date must be after start date')
        return v

class LeaveRequestCreate(LeaveRequestBase):
    pass

class LeaveRequestUpdate(BaseModel):
    status: Optional[LeaveStatus] = None
    rejection_reason: Optional[str] = None
    approved_by: Optional[int] = None

class LeaveRequestResponse(LeaveRequestBase):
    id: int
    employee_id: int
    employee_name: str
    status: LeaveStatus
    rejection_reason: Optional[str] = None
    leave_duration: int
    created_at: datetime
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True

# Late Attendance schemas
class LateAttendanceBase(BaseModel):
    date: date
    check_in_time: Optional[datetime] = None
    late_reason: Optional[str] = None

class LateAttendanceCreate(LateAttendanceBase):
    employee_id: int
    employee_name: str

class LateAttendanceResponse(LateAttendanceBase):
    id: int
    employee_id: int
    employee_name: str
    approved_by: Optional[int] = None
    approved_at: Optional[datetime] = None
    created_at: datetime
    
    class Config:
        from_attributes = True

# Dashboard schemas
class DashboardStats(BaseModel):
    total_employees: int
    pending_leaves: int
    approved_leaves: int
    rejected_leaves: int
    late_attendances: int

class EmployeeDashboard(BaseModel):
    user: UserResponse
    recent_leaves: List[LeaveRequestResponse]
    leave_balance: dict
    upcoming_leaves: List[LeaveRequestResponse]

class HRDashboard(BaseModel):
    stats: DashboardStats
    pending_leaves: List[LeaveRequestResponse]
    recent_leaves: List[LeaveRequestResponse]
    late_attendances: List[LateAttendanceResponse]
