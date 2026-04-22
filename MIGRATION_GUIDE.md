# LeaveEase Migration Guide
## From Spring Boot/MongoDB to FastAPI/PostgreSQL

This guide documents the migration of LeaveEase from a Java Spring Boot + MongoDB stack to a Python FastAPI + PostgreSQL stack.

## Technology Stack Changes

### Old Stack
- **Backend**: Java 17, Spring Boot 3.5
- **Database**: MongoDB
- **Frontend**: Static HTML/CSS/JS
- **Authentication**: Spring Security + JWT

### New Stack
- **Backend**: Python 3.11, FastAPI
- **Database**: PostgreSQL 15
- **Frontend**: React 19 + Node.js
- **Authentication**: FastAPI JWT + Bearer tokens

## Project Structure

### New Backend Structure (`backend_fastapi/`)
```
backend_fastapi/
|-- app/
|   |-- __init__.py
|   |-- main.py              # FastAPI application entry point
|   |-- database.py          # Database configuration
|   |-- models.py            # SQLAlchemy models
|   |-- schemas.py           # Pydantic schemas
|   |-- auth.py              # Authentication utilities
|   |-- routers/
|       |-- __init__.py
|       |-- auth.py          # Authentication endpoints
|       |-- leaves.py        # Leave management endpoints
|       |-- users.py         # User management endpoints
|       |-- dashboard.py     # Dashboard endpoints
|-- requirements.txt         # Python dependencies
|-- Dockerfile              # Docker configuration
```

### New Frontend Structure (`frontend/`)
```
frontend/
|-- public/
|   |-- index.html
|-- src/
|   |-- components/
|   |   |-- Login.js
|   |   |-- Dashboard.js
|   |   |-- LeaveRequest.js
|   |-- contexts/
|   |   |-- AuthContext.js
|   |-- App.js
|   |-- index.js
|   |-- index.css
|-- package.json
|-- Dockerfile
```

## Key Changes

### 1. Database Migration
- **From**: MongoDB with document collections
- **To**: PostgreSQL with relational tables

#### Schema Changes:
- `users` collection -> `users` table with proper relationships
- `leave_requests` collection -> `leave_requests` table with foreign keys
- Added proper constraints and indexes for performance

### 2. API Migration
- **From**: Spring Boot REST controllers
- **To**: FastAPI routers with Pydantic models

#### Endpoint Mapping:
| Old Endpoint | New Endpoint | Method |
|---------------|--------------|--------|
| `/auth/login` | `/auth/login` | POST |
| `/auth/current-user` | `/auth/current-user` | GET |
| `/leave/apply` | `/leaves/` | POST |
| `/leave/status` | `/leaves/` | GET |
| `/leave/approve/{id}` | `/leaves/{id}/approve` | PUT |
| `/leave/reject/{id}` | `/leaves/{id}/reject` | PUT |

### 3. Authentication Migration
- **From**: Spring Security with JWT
- **To**: FastAPI with JWT Bearer tokens
- Maintains the same token-based approach but with Python implementation

### 4. Frontend Migration
- **From**: Static HTML pages with vanilla JavaScript
- **To**: React SPA with component-based architecture
- Improved user experience with better state management

## Running the New Stack

### Using Docker Compose
```bash
# Use the new docker-compose file
docker-compose -f docker-compose-new.yml up -d
```

### Manual Setup
```bash
# Backend
cd backend_fastapi
pip install -r requirements.txt
uvicorn app.main:app --reload

# Frontend
cd frontend
npm install
npm start
```

## Environment Variables

### Backend
- `DATABASE_URL`: PostgreSQL connection string
- `SECRET_KEY`: JWT secret key

### Frontend
- `REACT_APP_API_URL`: Backend API URL

## Data Migration

To migrate existing data from MongoDB to PostgreSQL:

1. Export data from MongoDB
2. Transform data to match new schema
3. Import into PostgreSQL using scripts or tools

## Benefits of Migration

1. **Better Performance**: PostgreSQL with proper indexing
2. **Modern Frontend**: React provides better UX
3. **Type Safety**: Pydantic schemas for API validation
4. **Easier Deployment**: Docker containers for all services
5. **Better Developer Experience**: FastAPI's automatic documentation

## Next Steps

1. Test all API endpoints
2. Verify frontend functionality
3. Migrate production data
4. Update CI/CD pipelines
5. Deploy to production

## Troubleshooting

### Common Issues
- **Database Connection**: Ensure PostgreSQL is running and credentials are correct
- **CORS Issues**: Check FastAPI CORS configuration
- **Authentication**: Verify JWT secret key matches between services

### Port Mapping
- Frontend: http://localhost:3000
- Backend: http://localhost:8000
- Database: localhost:5432
