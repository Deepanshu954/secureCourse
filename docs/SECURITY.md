# Security Documentation

## Security Posture

SecureCourse is an **educational security platform** designed to demonstrate web vulnerabilities in a controlled environment. The application intentionally implements both secure and vulnerable code paths, controlled by runtime toggles.

**⚠️ WARNING**: This application contains intentional vulnerabilities and should **NEVER** be deployed to production or exposed to the internet.

## Security Features

### 1. SQL Injection Protection

#### Secure Mode (Toggle ON)

**Implementation:**
```java
// Uses JPA/PreparedStatement with parameterized queries
User user = userRepository.findByUsername(username);
if (user != null && passwordEncoder.matches(password, user.getPassword())) {
    return user;
}
```

**Protection Mechanisms:**
- Parameterized queries via JPA
- BCrypt password hashing
- No string concatenation in SQL

#### Vulnerable Mode (Toggle OFF)

**Implementation:**
```java
// Raw SQL with string concatenation
String sql = "SELECT * FROM users WHERE username='" + username + 
             "' AND password='" + password + "'";
Query query = entityManager.createNativeQuery(sql, User.class);
```

**Vulnerability:**
- Direct string concatenation allows SQL injection
- Attacker can bypass authentication with: `' OR '1'='1`
- Password check is bypassed

**Attack Scenario:**
1. Turn OFF SQL Injection Protection toggle
2. Login with username: `' OR '1'='1`
3. Any password will authenticate as the first user in database

**Mitigation (When ON):**
- Always use PreparedStatements or JPA
- Never concatenate user input into SQL queries
- Use parameterized queries: `WHERE username = ?`

---

### 2. Cross-Site Scripting (XSS) Protection

#### Stored XSS

**Secure Mode (Toggle ON):**
```java
import org.owasp.encoder.Encode;

String safeContent = Encode.forHtml(content);
comment.setContent(safeContent);
```

**Protection Mechanisms:**
- OWASP Java Encoder for HTML encoding
- Converts `<` to `&lt;`, `>` to `&gt;`
- Prevents script execution

**Vulnerable Mode (Toggle OFF):**
```java
// Raw HTML stored without encoding
comment.setContent(content);
```

**Vulnerability:**
- Raw HTML/JavaScript stored in database
- Executed when rendered in browser
- Affects all users viewing the comment

**Attack Scenario:**
1. Turn OFF XSS Protection toggle
2. Post comment: `<img src=x onerror=alert('XSS')>`
3. Script executes when any user views the page

**Frontend Rendering:**
```jsx
// Intentionally uses dangerouslySetInnerHTML to demonstrate XSS
<div dangerouslySetInnerHTML={{ __html: comment.content }} />
```

**Mitigation (When ON):**
- Always encode user input before storage
- Use OWASP Encoder or similar libraries
- Implement Content Security Policy (CSP)
- Avoid `dangerouslySetInnerHTML` in production

#### Reflected XSS
Currently not implemented in this demo.

#### DOM-Based XSS
Currently not implemented in this demo.

---

### 3. File Upload Security

#### Secure Mode (Toggle ON)

**Validation Pipeline:**
```java
private void validateFile(MultipartFile file) {
    String contentType = file.getContentType();
    String filename = file.getOriginalFilename();
    
    // MIME type validation
    if (!contentType.equals("image/png") && 
        !contentType.equals("image/jpeg")) {
        throw new RuntimeException("Invalid file type");
    }
    
    // Extension validation
    String extension = filename.substring(
        filename.lastIndexOf(".") + 1).toLowerCase();
    if (!extension.equals("png") && 
        !extension.equals("jpg") && 
        !extension.equals("jpeg")) {
        throw new RuntimeException("Invalid extension");
    }
}

// UUID-based filename
String filename = UUID.randomUUID().toString() + "_" + 
                  file.getOriginalFilename();
```

**Protection Mechanisms:**
- MIME type whitelist (PNG, JPG only)
- File extension validation
- UUID prefix prevents filename collisions
- Path traversal protection

#### Vulnerable Mode (Toggle OFF)

**Implementation:**
```java
// Trusts user input completely
String filename = file.getOriginalFilename();
// No validation, accepts any file type
```

**Vulnerabilities:**
- Arbitrary file upload (HTML, JS, executable files)
- Original filename preserved (potential path traversal)
- No content validation

**Attack Scenarios:**

1. **Malicious HTML Upload:**
   - Upload `exploit.html` containing JavaScript
   - Access via `/course/files/exploit.html`
   - Script executes in browser context

2. **Path Traversal Attempt:**
   - Upload file named `../../evil.jsp`
   - May write outside intended directory

**Mitigation (When ON):**
- Whitelist allowed file types
- Validate both MIME type and extension
- Generate random filenames (UUID)
- Store files outside web root
- Implement virus scanning for production

---

### 4. Session Security

**Implementation:**
- Spring Security session management
- HTTP-only session cookies
- Session invalidation on logout
- BCrypt password hashing (cost factor 10)

**Session Lifecycle:**
1. Login → Create session → Set `JSESSIONID` cookie
2. Subsequent requests → Validate session
3. Logout → Invalidate session → Clear cookie

**Security Measures:**
- Passwords never stored in plaintext
- BCrypt with salt for password hashing
- Session-based authentication (no JWT in this demo)

**Limitations:**
- CSRF protection disabled for demo simplicity
- No session timeout configured
- No concurrent session control

---

## Threat Model

### Attack Surface

| Component | Attack Vector | Risk Level | Toggle |
|-----------|---------------|------------|--------|
| Login Form | SQL Injection | **HIGH** | sqlInjectionProtection |
| File Upload | Malicious Files | **HIGH** | fileUploadSecurity |
| Comments | Stored XSS | **HIGH** | xssProtection |
| Session | Session Hijacking | **MEDIUM** | N/A |

### Attacker Goals

1. **Bypass Authentication**: SQL injection to login without credentials
2. **Execute JavaScript**: XSS to steal cookies or deface page
3. **Upload Malware**: Upload malicious files to compromise server
4. **Data Exfiltration**: Access other users' data

### Defense Layers

```
┌─────────────────────────────────────────┐
│  Layer 1: Input Validation              │
│  - MIME type checks                     │
│  - Extension validation                 │
└─────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│  Layer 2: Business Logic Security       │
│  - Toggle-based security controls       │
│  - Parameterized queries                │
│  - HTML encoding                        │
└─────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│  Layer 3: Data Access Security          │
│  - JPA/Hibernate                        │
│  - Prepared statements                  │
└─────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│  Layer 4: Database Security             │
│  - MySQL access controls                │
│  - Encrypted passwords (BCrypt)         │
└─────────────────────────────────────────┘
```

---

## Common Attack Scenarios

### Scenario 1: SQL Injection Attack

**Prerequisites:**
- SQL Injection Protection toggle is OFF

**Attack Steps:**
1. Navigate to login page
2. Enter username: `' OR '1'='1 --`
3. Enter any password
4. Click Login

**Result:**
- Bypasses authentication
- Logs in as first user (admin)

**SQL Executed:**
```sql
SELECT * FROM users WHERE username='' OR '1'='1 --' AND password='anything'
```

**Explanation:**
- `' OR '1'='1` always evaluates to true
- `--` comments out the rest of the query
- Password check is bypassed

---

### Scenario 2: Stored XSS Attack

**Prerequisites:**
- XSS Protection toggle is OFF

**Attack Steps:**
1. Navigate to course view
2. Post comment: `<img src=x onerror=alert(document.cookie)>`
3. Comment is stored in database
4. Any user viewing the page triggers the script

**Result:**
- JavaScript executes in victim's browser
- Can steal cookies, redirect, or modify page

**Payload Variations:**
```html
<script>alert('XSS')</script>
<img src=x onerror=alert(1)>
<svg onload=alert(1)>
<iframe src="javascript:alert(1)">
```

---

### Scenario 3: Malicious File Upload

**Prerequisites:**
- File Upload Security toggle is OFF

**Attack Steps:**
1. Create malicious HTML file:
```html
<html>
<body>
<script>alert('Malicious file executed!');</script>
</body>
</html>
```
2. Upload file to course
3. Access file via `/course/files/malicious.html`

**Result:**
- HTML file executes in browser
- Can perform phishing, XSS, or other attacks

---

## Security Best Practices

### For Production Deployment

1. **Always Enable All Toggles**: Keep all security protections ON
2. **Add CSRF Protection**: Enable Spring Security CSRF tokens
3. **Implement Rate Limiting**: Prevent brute force attacks
4. **Add Input Validation**: Validate all user inputs
5. **Use HTTPS**: Encrypt all traffic
6. **Implement CSP**: Content Security Policy headers
7. **Add Logging**: Log all security events
8. **Regular Updates**: Keep dependencies updated
9. **Security Headers**: Add X-Frame-Options, X-Content-Type-Options
10. **Virus Scanning**: Scan uploaded files

### Code Review Checklist

- [ ] No SQL string concatenation
- [ ] All user input is validated
- [ ] HTML output is encoded
- [ ] File uploads are validated
- [ ] Passwords are hashed with BCrypt
- [ ] Sessions are properly managed
- [ ] Error messages don't leak information
- [ ] HTTPS is enforced
- [ ] CSRF protection is enabled

---

## Privacy Considerations

### Data Storage

- **Passwords**: Hashed with BCrypt (never plaintext)
- **Session Data**: Stored in memory (not persisted)
- **Uploaded Files**: Stored in local filesystem
- **Comments**: Stored in MySQL database

### Data Retention

- User data persists until manually deleted
- Sessions expire on logout or browser close
- Uploaded files persist indefinitely

### GDPR Considerations

For production use, implement:
- Right to erasure (delete user data)
- Data export functionality
- Privacy policy
- Cookie consent
- Data encryption at rest

---

## Compliance Notes

This application is for **educational purposes only** and does not comply with:
- PCI DSS (payment card security)
- HIPAA (health information)
- SOC 2 (service organization controls)
- GDPR (data protection) - partial compliance only

**Do not use for production systems handling sensitive data.**
