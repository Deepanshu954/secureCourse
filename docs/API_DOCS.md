# API Documentation

## Base URL
```
http://localhost:8080
```

## Authentication

All authenticated endpoints require a valid session cookie (`JSESSIONID`).

---

## Auth Endpoints

### POST /auth/signup
Create a new user account.

**Request:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response (200 OK):**
```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

**Error (400 Bad Request):**
```json
{
  "error": "Username already exists"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

---

### POST /auth/login
Authenticate user and create session.

**Request:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response (200 OK):**
```json
{
  "message": "Login successful",
  "username": "admin",
  "role": "USER"
}
```

**Response Headers:**
```
Set-Cookie: JSESSIONID=ABC123...
```

**Error (401 Unauthorized):**
```json
{
  "error": "Invalid credentials"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"username":"admin","password":"password"}'
```

**Security Note:**
- When `sqlInjectionProtection` is **OFF**, the login is vulnerable to SQL injection
- Try username: `' OR '1'='1` with any password to bypass authentication

---

### POST /auth/logout
Invalidate current session.

**Request:** (No body required)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/auth/logout \
  -b cookies.txt
```

---

### GET /auth/me
Get current authenticated user.

**Response (200 OK):**
```json
{
  "username": "admin",
  "id": 1
}
```

**Error (401 Unauthorized):**
```json
{
  "error": "Not authenticated"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/auth/me \
  -b cookies.txt
```

---

## Course Endpoints

### GET /course
List all courses.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Cybersecurity 101",
    "description": "Introduction to Web Security"
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/course
```

---

### GET /course/{id}/files
Get all files for a specific course.

**Path Parameters:**
- `id` (integer): Course ID

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "originalFilename": "screenshot.png",
    "storedFilename": "a1b2c3d4-e5f6-7890-abcd-ef1234567890_screenshot.png",
    "contentType": "image/png",
    "size": 245678,
    "course": {
      "id": 1,
      "title": "Cybersecurity 101",
      "description": "Introduction to Web Security"
    }
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/course/1/files
```

---

### POST /course/upload
Upload a file to a course.

**Request (multipart/form-data):**
- `file`: File to upload
- `courseId`: Course ID (integer)

**Response (200 OK):**
```json
{
  "id": 1,
  "originalFilename": "image.png",
  "storedFilename": "uuid_image.png",
  "contentType": "image/png",
  "size": 123456
}
```

**Error (400 Bad Request):**
```json
{
  "error": "Invalid file type. Only PNG and JPG images are allowed."
}
```

**Validation Rules (when fileUploadSecurity is ON):**
- Content-Type must be `image/png` or `image/jpeg`
- File extension must be `.png`, `.jpg`, or `.jpeg`
- Filename is sanitized with UUID prefix

**cURL Example:**
```bash
curl -X POST http://localhost:8080/course/upload \
  -F "file=@/path/to/image.png" \
  -F "courseId=1" \
  -b cookies.txt
```

---

### GET /course/files/{filename}
Serve uploaded file.

**Path Parameters:**
- `filename` (string): Stored filename

**Response:** File content with appropriate Content-Type header

**cURL Example:**
```bash
curl -X GET http://localhost:8080/course/files/uuid_image.png \
  --output downloaded_image.png
```

---

## Comment Endpoints

### POST /course/{id}/comment
Add a comment to a course.

**Path Parameters:**
- `id` (integer): Course ID

**Request:**
```json
{
  "content": "This is a great course!",
  "author": "username"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "content": "This is a great course!",
  "author": "username",
  "createdAt": "2025-11-19T23:45:00",
  "course": {
    "id": 1,
    "title": "Cybersecurity 101"
  }
}
```

**Security Note:**
- When `xssProtection` is **ON**, HTML content is encoded
- When `xssProtection` is **OFF**, raw HTML is stored (XSS vulnerability)

**cURL Example:**
```bash
curl -X POST http://localhost:8080/course/1/comment \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"content":"Great course!","author":"admin"}'
```

---

### GET /course/{id}/comment
Get all comments for a course.

**Path Parameters:**
- `id` (integer): Course ID

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "content": "This is a great course!",
    "author": "admin",
    "createdAt": "2025-11-19T23:45:00"
  }
]
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/course/1/comment
```

---

### DELETE /course/{courseId}/comments/reset
Delete all comments for a course.

**Path Parameters:**
- `courseId` (integer): Course ID

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Comments reset"
}
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/course/1/comments/reset \
  -b cookies.txt
```

---

## Toggle Endpoints

### GET /toggles
Get current state of all security toggles.

**Response (200 OK):**
```json
{
  "sqlInjectionProtection": true,
  "fileUploadSecurity": true,
  "xssProtection": true
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/toggles
```

---

### POST /toggles/update
Update a specific security toggle.

**Request:**
```json
{
  "toggleName": "sqlInjectionProtection",
  "enabled": false
}
```

**Valid Toggle Names:**
- `sqlInjectionProtection`
- `fileUploadSecurity`
- `xssProtection`

**Response (200 OK):**
```json
{
  "sqlInjectionProtection": false,
  "fileUploadSecurity": true,
  "xssProtection": true
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/toggles/update \
  -H "Content-Type: application/json" \
  -d '{"toggleName":"sqlInjectionProtection","enabled":false}'
```

---

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | Success |
| 400 | Bad Request - Invalid input or validation error |
| 401 | Unauthorized - Authentication required |
| 404 | Not Found - Resource doesn't exist |
| 500 | Internal Server Error |

## Common Error Response Format

```json
{
  "error": "Error message description"
}
```

## CORS Configuration

The backend allows requests from:
- `http://localhost:5173` (development frontend)

Credentials are enabled for session cookie support.

## Rate Limiting

Currently, no rate limiting is implemented. For production deployment, consider adding rate limiting middleware.

## Pagination

Currently, no pagination is implemented. All list endpoints return complete datasets.
