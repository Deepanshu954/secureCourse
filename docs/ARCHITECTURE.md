# System Architecture

## High-Level Architecture

SecureCourse follows a three-tier architecture pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer (Browser)                   │
│                    React SPA (Port 5173)                     │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/REST
                         │ (Axios)
┌────────────────────────▼────────────────────────────────────┐
│                  Application Layer                           │
│              Spring Boot (Port 8080)                         │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Controllers → Services → Repositories               │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────┬────────────────────────────────────┘
                         │ JDBC
                         │ (Hibernate/JPA)
┌────────────────────────▼────────────────────────────────────┐
│                    Data Layer                                │
│                 MySQL Database                               │
│         (users, courses, files, comments)                    │
└─────────────────────────────────────────────────────────────┘
```

## Backend Architecture

### Layer Structure

```
com.securecourse.backend/
├── auth/                      # Authentication Module
│   ├── User.java             # Entity
│   ├── UserRepository.java   # Data Access
│   ├── AuthService.java      # Business Logic
│   └── AuthController.java   # REST API
├── course/                    # Course & File Module
│   ├── Course.java
│   ├── FileMetadata.java
│   ├── CourseRepository.java
│   ├── FileMetadataRepository.java
│   ├── FileService.java
│   └── CourseController.java
├── comments/                  # Comments Module
│   ├── Comment.java
│   ├── CommentRepository.java
│   ├── CommentService.java
│   └── CommentController.java
├── toggles/                   # Security Toggle Module
│   ├── ToggleService.java
│   └── ToggleController.java
└── config/                    # Configuration
    ├── SecurityConfig.java
    └── DataSeeder.java
```

### Layer Responsibilities

#### 1. Controller Layer
- **Purpose**: Handle HTTP requests/responses
- **Responsibilities**:
  - Request validation
  - Response formatting
  - HTTP status code management
  - CORS configuration
- **Example**: `AuthController` handles `/auth/login`, `/auth/signup`, `/auth/logout`

#### 2. Service Layer
- **Purpose**: Business logic and security controls
- **Responsibilities**:
  - Toggle-based security logic
  - Password hashing (BCrypt)
  - XSS encoding (OWASP Encoder)
  - File validation
- **Example**: `AuthService.login()` implements both safe and unsafe SQL modes

#### 3. Repository Layer
- **Purpose**: Data persistence
- **Responsibilities**:
  - JPA entity management
  - Custom queries
  - Transaction management
- **Example**: `UserRepository.findByUsername()` for authentication

#### 4. Entity Layer
- **Purpose**: Data models
- **Responsibilities**:
  - JPA annotations
  - Relationships (OneToMany, ManyToOne)
  - Constraints (unique, nullable)

## Frontend Architecture

### Component Structure

```
src/
├── components/
│   ├── Navbar.jsx              # Navigation bar
│   └── SecurityTogglePanel.jsx # Floating toggle panel
├── pages/
│   ├── Login.jsx               # Login page
│   ├── Signup.jsx              # Signup page
│   ├── Dashboard.jsx           # Course listing
│   └── CourseView.jsx          # Course details + upload + comments
├── context/
│   ├── AuthContext.jsx         # Global auth state
│   └── ToggleContext.jsx       # Global toggle state
├── services/
│   └── api.js                  # Axios instance
├── App.jsx                     # Router configuration
├── main.jsx                    # Entry point
└── index.css                   # Global styles
```

### State Management

#### AuthContext
- **State**: `user`, `loading`
- **Methods**: `login()`, `signup()`, `logout()`, `checkAuth()`
- **Purpose**: Manage authentication state across the app

#### ToggleContext
- **State**: `toggles` (sqlInjectionProtection, fileUploadSecurity, xssProtection)
- **Methods**: `updateToggle()`
- **Purpose**: Synchronize security toggles with backend

### Routing

```javascript
/                    → Redirect to /dashboard
/login               → Login page
/signup              → Signup page
/dashboard           → Course listing (Protected)
/course/:id          → Course view (Protected)
```

## Authentication Flow

```
┌──────────┐                ┌──────────┐                ┌──────────┐
│  Client  │                │  Backend │                │ Database │
└────┬─────┘                └────┬─────┘                └────┬─────┘
     │                           │                           │
     │ POST /auth/login          │                           │
     │ {username, password}      │                           │
     ├──────────────────────────>│                           │
     │                           │                           │
     │                           │ SELECT * FROM users       │
     │                           │ WHERE username=?          │
     │                           ├──────────────────────────>│
     │                           │                           │
     │                           │<──────────────────────────┤
     │                           │ User record               │
     │                           │                           │
     │                           │ BCrypt.matches(password)  │
     │                           │                           │
     │                           │ session.setAttribute()    │
     │                           │                           │
     │<──────────────────────────┤                           │
     │ 200 OK + Session Cookie   │                           │
     │                           │                           │
     │ GET /auth/me              │                           │
     ├──────────────────────────>│                           │
     │                           │                           │
     │                           │ session.getAttribute()    │
     │                           │                           │
     │<──────────────────────────┤                           │
     │ {username, id}            │                           │
     │                           │                           │
```

## File Upload Flow

```
┌──────────┐                ┌──────────┐                ┌────────────┐
│  Client  │                │  Backend │                │ Filesystem │
└────┬─────┘                └────┬─────┘                └─────┬──────┘
     │                           │                            │
     │ POST /course/upload       │                            │
     │ FormData(file, courseId)  │                            │
     ├──────────────────────────>│                            │
     │                           │                            │
     │                           │ if (toggleService.         │
     │                           │   isFileUploadSecure())    │
     │                           │                            │
     │                           │ validateFile()             │
     │                           │ - Check MIME type          │
     │                           │ - Check extension          │
     │                           │                            │
     │                           │ filename = UUID + original │
     │                           │                            │
     │                           │ Files.copy()               │
     │                           ├───────────────────────────>│
     │                           │                            │
     │                           │ Save FileMetadata to DB    │
     │                           │                            │
     │<──────────────────────────┤                            │
     │ 200 OK {fileMetadata}     │                            │
     │                           │                            │
```

## Security Toggle Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    ToggleService                         │
│  ┌────────────────────────────────────────────────────┐ │
│  │  In-Memory State:                                  │ │
│  │  - sqlInjectionProtection: boolean (default true) │ │
│  │  - fileUploadSecurity: boolean (default true)     │ │
│  │  - xssProtection: boolean (default true)          │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                         │
                         │ Injected into
                         ▼
        ┌────────────────────────────────────┐
        │  AuthService / FileService /       │
        │  CommentService                    │
        │                                    │
        │  if (toggleService.isEnabled()) {  │
        │      // Secure implementation      │
        │  } else {                          │
        │      // Vulnerable implementation  │
        │  }                                 │
        └────────────────────────────────────┘
```

### Toggle Flow

1. **Frontend**: User clicks toggle in `SecurityTogglePanel`
2. **API Call**: `PUT /toggles/update` with toggle name and state
3. **Backend**: `ToggleService` updates in-memory state
4. **Response**: Returns updated toggle states
5. **Frontend**: `ToggleContext` updates global state
6. **Effect**: All subsequent requests use new security mode

## Database Schema Overview

```
┌──────────────┐         ┌──────────────┐
│    users     │         │   courses    │
├──────────────┤         ├──────────────┤
│ id (PK)      │         │ id (PK)      │
│ username     │         │ title        │
│ password     │         │ description  │
└──────────────┘         └──────┬───────┘
                                │
                                │ 1:N
                    ┌───────────┴───────────┐
                    │                       │
          ┌─────────▼────────┐    ┌────────▼─────────┐
          │  file_metadata   │    │     comment      │
          ├──────────────────┤    ├──────────────────┤
          │ id (PK)          │    │ id (PK)          │
          │ course_id (FK)   │    │ course_id (FK)   │
          │ original_filename│    │ content          │
          │ stored_filename  │    │ author           │
          │ content_type     │    │ created_at       │
          │ size             │    └──────────────────┘
          └──────────────────┘
```

## Performance Considerations

- **Connection Pooling**: HikariCP for database connections
- **Lazy Loading**: JPA lazy loading for entity relationships
- **Session Management**: HTTP sessions with cookie-based authentication
- **File Storage**: Local filesystem (uploads directory)
- **Frontend Optimization**: Vite's hot module replacement (HMR)

## Security Architecture

### Defense Layers

1. **Input Validation**: Controller-level validation
2. **Business Logic Security**: Service-level toggle checks
3. **Data Access Security**: JPA parameterized queries
4. **Output Encoding**: XSS protection via OWASP Encoder
5. **Session Security**: Spring Security session management

### Attack Surface

- **Login Endpoint**: SQL Injection (when toggle OFF)
- **File Upload**: Malicious file upload (when toggle OFF)
- **Comments**: Stored XSS (when toggle OFF)

See [SECURITY.md](./SECURITY.md) for detailed security documentation.
