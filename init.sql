-- Initialize LeaveEase PostgreSQL database

-- Create database (handled by docker-entrypoint)
-- CREATE DATABASE leavedb;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create indexes for better performance
-- These will be created by SQLAlchemy models

-- Insert sample data (optional - can be done via API)
-- This is just a placeholder for any initial data setup
