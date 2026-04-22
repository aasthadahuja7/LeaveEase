# LeaveEase — Modern Leave Management System

A modern, role-based leave management platform for HR teams and employees with smart email workflows, analytics, and optional AI assistance.

## 🚀 Features

### 👤 Role-Based Dashboards

#### HR Panel
- Approve/reject leave requests with reasons
- View pending approvals, presence, and leave statistics
- Manage late attendance records

#### Employee Panel
- Submit leave requests
- Track status and history
- View personal attendance insights

### 🔐 Authentication & Access
- JWT-based authentication with Bearer tokens
- Role-based access control (HR, Employee)
- Secure endpoints and session handling
  
### ✉️ Email & AI Workflows
- SMTP Email via Gmail(configurable)
- AI-Generated Emails via OpenAI or local AI (Ollama)
- N8N Workflow Integration for notifications
- Smart, configurable templates for HR and employee communications

### 📦 Data & Integrations
- PostgreSQL database with proper relationships
- RESTful API with automatic documentation
- Data validation and type safety
- Demo data initialization

## 🏗️ Technology Stack

- **Backend**: Python FastAPI 0.104.1, PostgreSQL 15
- **Database**: PostgreSQL
- **Email**: SMTP
- **Build Tool**: Docker & Docker Compose
- **UI**: React-based single-page application

## 📡 API Endpoints  
- `POST /auth/signup` → Register new user  
- `POST /auth/login` → User login  
- `POST /leave/apply` → Apply for leave  
- `GET /leave/status` → Check leave status  
- `PUT /leave/approve/{id}` → Approve leave (HR only)  
- `PUT /leave/reject/{id}` → Reject leave (HR only)  

## ⚙️ Getting Started

### Prerequisites

- Docker & Docker Compose
- Node.js 18+ (for local development)
- Python 3.11+ (for local development)

### Quick Start with Docker

1. **Clone the repository**
```bash
git clone <repo-url>
cd LeaveEase-main
```

2. **Start all services**
```bash
docker-compose -f docker-compose-new.yml up -d
```

3. **Initialize demo data**
```bash
docker-compose -f docker-compose-new.yml exec backend python app/init_data.py
```

4. **Access the application**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8000
- API Documentation: http://localhost:8000/docs

### Demo Accounts

- **HR User**: `hr_user` / `password123`
- **Employee**: `john_doe` / `password123`
- **Employee**: `jane_smith` / `password123`
- **Employee**: `mike_johnson` / `password123`

### Local Development Setup

#### Backend Setup
```bash
cd backend_fastapi
pip install -r requirements.txt
uvicorn app.main:app --reload
```

#### Frontend Setup
```bash
cd frontend
npm install
npm start
```

#### Database Setup
```bash
# Start PostgreSQL
docker run --name postgres -e POSTGRES_PASSWORD=leavepass123 -e POSTGRES_DB=leavedb -p 5432:5432 -d postgres:15

# Initialize data
cd backend_fastapi
python app/init_data.py
```

### Environment Variables

Create `.env` file in backend_fastapi/:
```
DATABASE_URL=postgresql://leaveuser:leavepass123@localhost:5432/leavedb
SECRET_KEY=your-secret-key-change-in-production
```

Create `.env` file in frontend/:
```
REACT_APP_API_URL=http://localhost:8000
```

## 📂 Project Structure
```
LeaveEase-main/
|-- backend_fastapi/                   # FastAPI backend
|   |-- app/
|   |   |-- main.py                   # Application entry point
|   |   |-- models.py                  # SQLAlchemy models
|   |   |-- schemas.py                 # Pydantic schemas
|   |   |-- auth.py                    # Authentication utilities
|   |   |-- database.py                # Database configuration
|   |   |-- init_data.py               # Demo data initialization
|   |   |-- routers/                   # API routers
|   |   |   |-- auth.py                # Authentication endpoints
|   |   |   |-- leaves.py              # Leave management
|   |   |   |-- users.py               # User management
|   |   |   -- dashboard.py            # Dashboard endpoints
|   |-- requirements.txt               # Python dependencies
|   |-- Dockerfile                     # Docker configuration
|   -- test_api.py                    # API testing script
|-- frontend/                          # React frontend
|   |-- public/
|   |   -- index.html                  # HTML template
|   |-- src/
|   |   |-- components/                # React components
|   |   |   |-- Login.js               # Login page
|   |   |   |-- Dashboard.js           # Main dashboard
|   |   |   -- LeaveRequest.js         # Leave request form
|   |   |-- contexts/                  # React contexts
|   |   |   -- AuthContext.js          # Authentication context
|   |   |-- App.js                     # Main app component
|   |   |-- index.js                   # Entry point
|   |   -- index.css                   # Global styles
|   |-- package.json                   # Node.js dependencies
|   -- Dockerfile                     # Docker configuration
|-- docker-compose-new.yml             # New Docker Compose setup
|-- init.sql                          # Database initialization
|-- MIGRATION_GUIDE.md                # Migration documentation
|-- README.md                         # Project documentation
```
## 🎥 Demo Video
You can watch the working demo of this project here:  
👉 [Watch Demo on Google Drive](https://drive.google.com/file/d/1oXXMrHchS-BvEpSVY74Gfh16BnnbtRTD/view?usp=sharing)

### 🙌 Acknowledgements

- Inspired by real-world HR leave management systems.

- Thanks to open-source community & resources used during development.
  
### 🧑‍💻 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you’d like to change.

### 👩‍💻 Author
Developed by Aastha Dahuja

- 📧 Email: aasthadahuja07@gmail.com

- 💼 LinkedIn: https://www.linkedin.com/in/aasthadahuja/


