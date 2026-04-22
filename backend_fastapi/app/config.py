"""
Configuration settings for LeaveEase FastAPI application
"""

import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # Database
    database_url: str = "postgresql://leaveuser:leavepass123@localhost:5432/leavedb"
    
    # Security
    secret_key: str = "your-secret-key-change-in-production"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 30
    
    # CORS
    allowed_origins: list = ["*"]
    
    # Application
    app_name: str = "LeaveEase API"
    app_version: str = "2.0.0"
    debug: bool = False
    
    class Config:
        env_file = ".env"

# Create settings instance
settings = Settings()
