from sqlalchemy import Column, Integer, String, DateTime, Enum, Text, ForeignKey, Date, Boolean
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import enum

Base = declarative_base()

class Role(enum.Enum):
    EMPLOYEE = "EMPLOYEE"
    HR = "HR"
    ADMIN = "ADMIN"

class LeaveStatus(enum.Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    REJECTED = "REJECTED"

class LeaveType(enum.Enum):
    ANNUAL = "ANNUAL"
    SICK = "SICK"
    PERSONAL = "PERSONAL"
    MATERNITY = "MATERNITY"
    PATERNITY = "PATERNITY"

class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, index=True, nullable=False)
    email = Column(String(100), unique=True, index=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    full_name = Column(String(100), nullable=False)
    department = Column(String(100))
    employee_code = Column(String(20), unique=True, index=True)
    role = Column(Enum(Role), default=Role.EMPLOYEE, nullable=False)
    profile_picture = Column(String(255))
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    leave_requests = relationship("LeaveRequest", back_populates="employee")
    late_attendances = relationship("LateAttendance", back_populates="employee", foreign_keys="LateAttendance.employee_id")

class LeaveRequest(Base):
    __tablename__ = "leave_requests"
    
    id = Column(Integer, primary_key=True, index=True)
    employee_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    employee_name = Column(String(100), nullable=False)
    start_date = Column(Date, nullable=False)
    end_date = Column(Date, nullable=False)
    reason = Column(Text)
    status = Column(Enum(LeaveStatus), default=LeaveStatus.PENDING, nullable=False)
    leave_type = Column(Enum(LeaveType), default=LeaveType.ANNUAL, nullable=False)
    rejection_reason = Column(Text)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # Relationships
    employee = relationship("User", back_populates="leave_requests")
    
    @property
    def leave_duration(self):
        if self.start_date and self.end_date:
            return (self.end_date - self.start_date).days + 1
        return 0

class LateAttendance(Base):
    __tablename__ = "late_attendances"
    
    id = Column(Integer, primary_key=True, index=True)
    employee_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    employee_name = Column(String(100), nullable=False)
    date = Column(Date, nullable=False)
    check_in_time = Column(DateTime(timezone=True))
    late_reason = Column(Text)
    approved_by = Column(Integer, ForeignKey("users.id"))
    approved_at = Column(DateTime(timezone=True))
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    # Relationships
    employee = relationship("User", back_populates="late_attendances", foreign_keys=[employee_id])
    approver = relationship("User", foreign_keys=[approved_by])
