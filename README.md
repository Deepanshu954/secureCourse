# SecureCourse

> An educational platform demonstrating web security vulnerabilities through interactive toggles

## 🎯 Purpose

SecureCourse is a full-stack web application designed to teach developers about common web security vulnerabilities in a safe, controlled environment. It features a unique **Security Toggle System** that allows real-time switching between secure and vulnerable code paths.

## ⚡ Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 9.5+
- Maven 3.9+

### Run Backend
```bash
cd backend
./mvnw spring-boot:run
```
Backend: `http://localhost:8080`

### Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend: `http://localhost:5173`

### Default Login
- **Username**: `admin`
- **Password**: `password`

## 🔐 Security Demonstrations

| Vulnerability | Toggle | Demo |
|---------------|--------|------|
| SQL Injection | sqlInjectionProtection | Login bypass with `' OR '1'='1` |
| Stored XSS | xssProtection | Comment injection with `<img src=x onerror=alert('XSS')>` |
| File Upload | fileUploadSecurity | Upload malicious files (.html, .sh) |

## 🛠️ Tech Stack

**Backend**: Spring Boot 3.5.7, Java 17, MySQL, Hibernate  
**Frontend**: React 18, Vite 5, React Router, Axios

## 📚 Documentation

- [Complete Documentation](./docs/DOCUMENTATION.md) - Enterprise-grade technical documentation
- [Architecture](./docs/ARCHITECTURE.md) - System design and architecture
- [API Reference](./docs/API_DOCS.md) - Complete API documentation
- [Security](./docs/SECURITY.md) - Security features and threat model
- [Installation](./docs/INSTALLATION.md) - Detailed setup guide
- [Deployment](./docs/DEPLOYMENT.md) - Production deployment guide
- [Database Schema](./docs/DATABASE_SCHEMA.md) - Database structure

## ⚠️ Warning

**This application intentionally contains vulnerabilities for educational purposes.**  
**DO NOT deploy to production or expose to the internet.**

## 📁 Project Structure

```
secureCourse/
├── backend/          # Spring Boot application
├── frontend/         # React application
├── docs/             # Documentation
├── database/         # Database scripts
└── uploads/          # File upload directory
```

## 🎓 Learning Objectives

- Understand SQL injection and parameterized queries
- Learn about XSS attacks and HTML encoding
- Explore file upload vulnerabilities and validation
- Practice secure coding principles
- Experience defense-in-depth strategies

---

**For detailed technical documentation, see [docs/DOCUMENTATION.md](./docs/DOCUMENTATION.md)**
