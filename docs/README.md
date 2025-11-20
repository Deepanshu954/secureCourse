# SecureCourse

> A security-focused educational platform demonstrating common web vulnerabilities and their mitigations through interactive toggles.

## Overview

SecureCourse is a full-stack web application designed to educate developers about web security vulnerabilities. It features a unique **Security Toggle System** that allows users to enable or disable security protections in real-time, demonstrating the impact of common vulnerabilities like SQL Injection, Cross-Site Scripting (XSS), and insecure file uploads.

## Key Features

- **Interactive Security Toggles**: Real-time switching between secure and vulnerable modes
- **SQL Injection Demo**: Login bypass demonstration with prepared statements vs. string concatenation
- **XSS Protection Demo**: Stored XSS with HTML encoding vs. raw output
- **File Upload Security**: MIME type validation and UUID naming vs. trusting user input
- **Image Preview System**: Grid-based image display with modal preview
- **Persistent Storage**: MySQL database for users, courses, files, and comments
- **Session Management**: Secure authentication with BCrypt password hashing
- **Rich UI**: Modern dark-themed interface with glassmorphism effects

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 17
- **Database**: MySQL 9.5
- **ORM**: Hibernate/JPA
- **Security**: Spring Security, BCrypt
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite 5
- **Routing**: React Router DOM 6
- **HTTP Client**: Axios
- **Styling**: Vanilla CSS with CSS Variables

## Project Structure

```
secureCourse/
├── backend/                    # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/securecourse/backend/
│   │       ├── auth/          # Authentication module
│   │       ├── comments/      # Comments module
│   │       ├── config/        # Configuration classes
│   │       ├── course/        # Course & file upload module
│   │       └── toggles/       # Security toggle service
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── frontend/                   # React frontend
│   ├── src/
│   │   ├── components/        # Reusable components
│   │   ├── context/           # React contexts (Auth, Toggles)
│   │   ├── pages/             # Page components
│   │   ├── services/          # API service layer
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── docs/                       # Project documentation
└── uploads/                    # File upload directory (auto-created)
```

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 9.5+
- Maven 3.9+

### Running the Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend runs on: `http://localhost:8080`

### Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on: `http://localhost:5173`

### Default Credentials
- **Username**: `admin`
- **Password**: `password`

## Security Toggle System

The application features three independent security toggles:

### 1. SQL Injection Protection
- **ON**: Uses JPA/PreparedStatements with parameterized queries
- **OFF**: Uses raw SQL string concatenation (vulnerable to injection)

### 2. File Upload Security
- **ON**: Validates MIME types (PNG/JPG only), generates UUID filenames
- **OFF**: Accepts any file type, preserves original filenames

### 3. XSS Protection
- **ON**: Encodes HTML using OWASP Java Encoder
- **OFF**: Stores and renders raw HTML (vulnerable to stored XSS)

## Testing Vulnerabilities

### SQL Injection
1. Turn OFF "SQL Injection Protection" toggle
2. Login with username: `' OR '1'='1`
3. Any password will bypass authentication

### File Upload
1. Turn OFF "File Upload Security" toggle
2. Upload malicious files (.html, .sh, etc.)
3. Files are stored with original names

### XSS Attack
1. Turn OFF "XSS Protection" toggle
2. Post comment: `<img src=x onerror=alert('XSS')>`
3. Script executes when page loads

## Documentation

- [Architecture](./docs/ARCHITECTURE.md) - System design and architecture
- [API Documentation](./docs/API_DOCS.md) - Complete API reference
- [Security](./docs/SECURITY.md) - Security features and threat model
- [Installation](./docs/INSTALLATION.md) - Detailed setup instructions
- [Deployment](./docs/DEPLOYMENT.md) - Production deployment guide
- [Database Schema](./docs/DATABASE_SCHEMA.md) - Database structure
- [Complete Documentation](./docs/DOCUMENTATION.md) - Comprehensive project documentation

## Contributors

<!-- Add contributors here -->

## License

This project is for educational purposes only.

---

**⚠️ Warning**: This application intentionally contains vulnerabilities for educational purposes. Do not deploy to production or expose to the internet.
