# Local Development Setup (Without Docker)

This guide shows how to run LeaveEase locally without Docker.

## Prerequisites

- Python 3.11+
- Node.js 18+
- PostgreSQL 15+

## Step 1: Setup PostgreSQL

### Option A: Install PostgreSQL locally
```bash
# On Mac with Homebrew
brew install postgresql@15
brew services start postgresql@15

# Create database
createdb leavedb
createuser leaveuser
psql -d leavedb -c "ALTER USER leaveuser PASSWORD 'leavepass123';"
psql -d leavedb -c "GRANT ALL PRIVILEGES ON DATABASE leavedb TO leaveuser;"
```

### Option B: Use Docker for PostgreSQL only
```bash
# Run just PostgreSQL
docker run --name postgres-leaveease \
  -e POSTGRES_DB=leavedb \
  -e POSTGRES_USER=leaveuser \
  -e POSTGRES_PASSWORD=leavepass123 \
  -p 5432:5432 \
  -d postgres:15
```

## Step 2: Backend Setup

```bash
cd backend_fastapi

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Create environment file
cat > .env << EOF
DATABASE_URL=postgresql://leaveuser:leavepass123@localhost:5432/leavedb
SECRET_KEY=your-secret-key-change-in-production
DEBUG=True
EOF

# Initialize database
python app/init_data.py

# Start backend
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## Step 3: Frontend Setup

Open a new terminal:

```bash
cd frontend

# Install dependencies
npm install

# Create environment file
cat > .env << EOF
REACT_APP_API_URL=http://localhost:8000
EOF

# Start frontend
npm start
```

## Step 4: Access the Application

- Frontend: http://localhost:3000
- Backend API: http://localhost:8000
- API Documentation: http://localhost:8000/docs

## Demo Accounts

- HR User: `hr_user` / `password123`
- Employee: `john_doe` / `password123`

## Troubleshooting

### Database Connection Issues
```bash
# Check if PostgreSQL is running
pg_isready -h localhost -p 5432

# Test connection
psql -h localhost -U leaveuser -d leavedb
```

### Backend Issues
```bash
# Check Python version
python3 --version

# Check dependencies
pip list

# Test API
curl http://localhost:8000/health
```

### Frontend Issues
```bash
# Check Node.js version
node --version
npm --version

# Clear npm cache
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

## Stop Services

```bash
# Stop backend (Ctrl+C in terminal)
# Stop frontend (Ctrl+C in terminal)

# Stop PostgreSQL if using Docker
docker stop postgres-leaveease
docker rm postgres-leaveease

# Stop PostgreSQL if installed locally
brew services stop postgresql@15
```
