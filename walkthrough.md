# SecureCourse Walkthrough

## Overview
SecureCourse is a vulnerable web application designed to demonstrate common web security flaws and their mitigations. It features a **Security Toggle Panel** that allows you to switch defenses ON and OFF in real-time.

## Running the Application

### Prerequisites
- **MySQL Server** running on localhost:3306
- Database: `securecourse` (auto-created)
- MySQL user: `root` with no password (or update [application.properties])

### Backend
The backend runs on port `8080` with MySQL database.
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
The frontend runs on port `5173`.
```bash
cd frontend
npm run dev
```
Access the app at: [http://localhost:5173](http://localhost:5173)

## Default Credentials
- **Username**: `admin`
- **Password**: [password](file:///Users/deepanshu95/Documents/Antigravity/secureCourse/backend/src/main/java/com/securecourse/backend/config/SecurityConfig.java#15-19)

## Recent Fixes
- ✅ **Logout Fixed**: Now uses `api.post` for consistent base URL handling
- ✅ **File Upload**: Strict PNG/JPG only when security is ON
- ✅ **Image Display**: Uploaded images shown in grid with clickable preview modal
- ✅ **MySQL Database**: Persistent storage (replaces H2 in-memory)
- ✅ **Login/Signup**: Fixed database schema issues

## Vulnerability Demos

### 1. SQL Injection (Login Bypass)
**Location**: Login Page  
**Toggle**: `SQL Injection Protection`

- **Secure Mode (ON)**:
    - Username: `' OR '1'='1`
    - Password: `anything`
    - **Result**: Login Failed (Invalid credentials)
- **Vulnerable Mode (OFF)**:
    - Username: `' OR '1'='1`
    - Password: `anything`
    - **Result**: Login Successful! Bypassed authentication

### 2. Malicious File Upload
**Location**: Course View → Upload File  
**Toggle**: `File Upload Security`

- **Secure Mode (ON)**:
    - Only `.png` and `.jpg` files allowed
    - Files saved with UUID prefix
    - **Result**: Non-image files rejected
- **Vulnerable Mode (OFF)**:
    - Any file type accepted
    - Original filename preserved
    - **Result**: Can upload [.html](file:///Users/deepanshu95/Documents/Antigravity/secureCourse/frontend/index.html), `.sh`, etc.

### 3. Stored XSS (Cross-Site Scripting)
**Location**: Course View → Comments  
**Toggle**: `XSS Protection`

- **Secure Mode (ON)**:
    - Comment: `<img src=x onerror=alert('XSS')>`
    - **Result**: Displayed as text (HTML encoded)
- **Vulnerable Mode (OFF)**:
    - Comment: `<img src=x onerror=alert('XSS')>`
    - **Result**: Alert box pops up! Script executed

## Features
- **Image Preview**: Click uploaded images to view full-size modal
- **Reset Comments**: Clear all comments for a course
- **Persistent Data**: MySQL database stores users, courses, files, comments

## Architecture
- **Backend**: Spring Boot (Java 17), MySQL, Spring Security
- **Frontend**: React (Vite), Vanilla CSS (Rich Aesthetics)
- **Security**:
    - **SQLi**: PreparedStatement vs String Concatenation
    - **XSS**: OWASP Java Encoder vs Raw Output
    - **Uploads**: MIME validation & UUIDs vs Trusting User Input

