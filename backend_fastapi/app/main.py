from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from typing import List, Optional
import uvicorn

from app.database import get_db, engine
from app.models import Base, User, LeaveRequest, LateAttendance
from app.schemas import UserCreate, UserResponse, LeaveRequestCreate, LeaveRequestResponse, LoginRequest, Token
from app.auth import authenticate_user, create_access_token, get_current_user
from app.routers import auth, leaves, users, dashboard
from app.config import settings

# Create database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title=settings.app_name,
    description="Modern Leave Management System",
    version=settings.app_version,
    debug=settings.debug
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(auth.router, prefix="/auth", tags=["authentication"])
app.include_router(leaves.router, prefix="/leaves", tags=["leaves"])
app.include_router(users.router, prefix="/users", tags=["users"])
app.include_router(dashboard.router, prefix="/dashboard", tags=["dashboard"])

@app.get("/")
async def root():
    return {"message": "LeaveEase API is running"}

@app.get("/health")
async def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
