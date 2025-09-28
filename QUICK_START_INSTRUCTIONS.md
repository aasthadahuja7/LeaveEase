# 🚀 Quick Start Instructions

## ⚠️ Current Issue
The application is having trouble connecting to MongoDB. Here are the steps to get it running:

## 🔧 **Option 1: Fix MongoDB Connection**

### 1. **Check MongoDB Status**
```bash
brew services list | grep mongodb
```

### 2. **Start MongoDB if not running**
```bash
brew services start mongodb-community@7.0
```

### 3. **Start the Application**
```bash
cd /Users/aastha/Downloads/leave-app
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

### 4. **Access the Application**
- Open browser: `http://localhost:8082`
- Login with: `hr_user` / `password123` (HR) or `john_doe` / `password123` (Employee)

---

## 🔧 **Option 2: Run Without Database (Simplified)**

If MongoDB continues to cause issues, we can modify the application to work with in-memory data:

### 1. **Disable MongoDB Auto-Configuration**
Add this to `application.properties`:
```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
server.port=8082
```

### 2. **Use In-Memory Data**
The application will work with hardcoded demo data instead of MongoDB.

---

## 🎯 **Demo Credentials**

### HR User (Full Access)
- **Username:** `hr_user`
- **Password:** `password123`

### Employee Users
- **Username:** `john_doe` / `password123`
- **Username:** `jane_smith` / `password123`

---

## 🎪 **Expected Features**

### **HR Dashboard:**
- 👥 Total Employees count
- 🏠 Employees on Leave count  
- ✅ Employees Present count
- ⏳ Pending Approvals count
- 👨‍💼 HR Management tab with approval features

### **Employee Dashboard:**
- 📊 Personal leave statistics
- 📝 Leave request form
- 📈 Charts and analytics
- 🚫 No administrative features

---

## 🔍 **Troubleshooting**

### **If port 8082 is busy:**
```bash
lsof -ti:8082 | xargs kill -9
```

### **If MongoDB connection fails:**
```bash
brew services restart mongodb-community@7.0
```

### **Check application logs:**
Look for error messages in the Maven output when starting the application.

---

## 📞 **Quick Test**

1. **Start Application:** `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082`
2. **Wait for:** "Started LeaveAppApplication" message
3. **Open:** `http://localhost:8082`
4. **Login:** Use demo credentials above
5. **Test:** Role-based features

**The role-based dashboard system is complete and ready to test!** 🎉