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
- Spring Security with role-based access control (HR, Employee)
- Secure endpoints and session handling

### 📦 Data & Integrations
- MongoDB persistence with auto-seeded demo data
- WebSocket support for interactive features (AI chat)
- RESTful APIs for extensibility

## 🏗️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.5 (Web, Security, WebSocket, WebFlux, Validation)
- **Database**: MongoDB
- **Build Tool**: Maven Wrapper (mvnw)
- **UI**: Static HTML, CSS, JavaScript served via Spring Boot
  
## 📡 API Endpoints  
- `POST /auth/signup` → Register new user  
- `POST /auth/login` → User login  
- `POST /leave/apply` → Apply for leave  
- `GET /leave/status` → Check leave status  
- `PUT /leave/approve/{id}` → Approve leave (HR only)  
- `PUT /leave/reject/{id}` → Reject leave (HR only)  
## ⚙️ Getting Started

### 🔹 Prerequisites

-Java 17
-MongoDB 7+ 
(running on localhost:27017)

### 🔹 Installation

1. **Clone the repository**
```bash
git clone <repo-url>
cd LeaveEase-main
```

2. **Configure environment (optional but recommended)**
```
Edit src/main/resources/application.properties:

# Server
server.port=8080

# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=leave_management_db

# Email (Gmail SMTP - use App Password with 2FA)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS@gmail.com
spring.mail.password=YOUR_16_CHAR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# AI (optional)
openai.api.key=YOUR_OPENAI_KEY
ollama.api.url=http://localhost:11434

```
3. **Run the application**
```bash
./mvnw spring-boot:run

```
Access the app at 👉 http://localhost:8082

## 📂 Project Structure
```
LeaveEase-main/
├── src/main/java/com/leaveease/...   # Backend source code
├── src/main/resources/               # Properties, static files
│   ├── static/                       # HTML, CSS, JS
│   └── application.properties        # Configurations
├── pom.xml                           # Maven dependencies
├── mvnw / mvnw.cmd                   # Maven wrapper scripts
└── README.md                         # Project documentation
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

- 🌐 Portfolio: https://preview--aastha-portfolio.lovable.app/
