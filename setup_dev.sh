#!/bin/bash

# LeaveEase Development Setup Script
# This script sets up the development environment for the FastAPI/React stack

set -e

echo "=== LeaveEase Development Setup ==="
echo "Setting up FastAPI + React development environment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Node.js is not installed. Please install Node.js 18+ first."
    exit 1
fi

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "Python 3 is not installed. Please install Python 3.11+ first."
    exit 1
fi

echo "All prerequisites found. Setting up development environment..."

# Create environment files
echo "Creating environment files..."

# Backend environment file
cat > backend_fastapi/.env << EOF
DATABASE_URL=postgresql://leaveuser:leavepass123@localhost:5432/leavedb
SECRET_KEY=your-secret-key-change-in-production-please
DEBUG=True
EOF

# Frontend environment file
cat > frontend/.env << EOF
REACT_APP_API_URL=http://localhost:8000
EOF

echo "Environment files created."

# Install Python dependencies
echo "Installing Python dependencies..."
cd backend_fastapi
python3 -m venv venv || echo "Virtual environment already exists"
source venv/bin/activate
pip install -r requirements.txt
cd ..

# Install Node.js dependencies
echo "Installing Node.js dependencies..."
cd frontend
npm install
cd ..

echo "Dependencies installed successfully!"

# Start services with Docker Compose
echo "Starting PostgreSQL database..."
docker-compose -f docker-compose-new.yml up -d postgres

# Wait for database to be ready
echo "Waiting for database to be ready..."
sleep 10

# Initialize database with demo data
echo "Initializing database with demo data..."
cd backend_fastapi
source venv/bin/activate
python app/init_data.py
cd ..

echo "Database initialized!"

# Test API endpoints
echo "Testing API endpoints..."
cd backend_fastapi
source venv/bin/activate
python test_api.py
cd ..

echo ""
echo "=== Setup Complete! ==="
echo ""
echo "To start the development servers:"
echo ""
echo "1. Backend (FastAPI):"
echo "   cd backend_fastapi"
echo "   source venv/bin/activate"
echo "   uvicorn app.main:app --reload"
echo ""
echo "2. Frontend (React):"
echo "   cd frontend"
echo "   npm start"
echo ""
echo "3. Or use Docker Compose for everything:"
echo "   docker-compose -f docker-compose-new.yml up"
echo ""
echo "Access the application:"
echo "- Frontend: http://localhost:3000"
echo "- Backend API: http://localhost:8000"
echo "- API Documentation: http://localhost:8000/docs"
echo ""
echo "Demo accounts:"
echo "- HR User: hr_user / password123"
echo "- Employee: john_doe / password123"
echo ""
echo "Happy coding! :)"
