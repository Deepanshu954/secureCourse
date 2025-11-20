# Database Schema

## Overview

SecureCourse uses MySQL as its relational database management system. The schema consists of four main tables representing users, courses, file metadata, and comments.

---

## Entity Relationship Diagram

```
┌──────────────────┐
│      users       │
├──────────────────┤
│ id (PK)          │
│ username (UQ)    │
│ password         │
└──────────────────┘
         
         
┌──────────────────┐         ┌──────────────────────┐
│     courses      │         │    file_metadata     │
├──────────────────┤         ├──────────────────────┤
│ id (PK)          │◄───────┤│ id (PK)              │
│ title            │   1:N   │ course_id (FK)       │
│ description      │         │ original_filename    │
└────────┬─────────┘         │ stored_filename      │
         │                   │ content_type         │
         │                   │ size                 │
         │                   └──────────────────────┘
         │
         │ 1:N
         │
         │         ┌──────────────────────┐
         └────────►│      comment         │
                   ├──────────────────────┤
                   │ id (PK)              │
                   │ course_id (FK)       │
                   │ content (TEXT)       │
                   │ author               │
                   │ created_at           │
                   └──────────────────────┘
```

---

## Table Definitions

### 1. users

Stores user authentication information.

```sql
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY UKr43af9ap4edm43mmtq01oddj6 (username)
) ENGINE=InnoDB;
```

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique user identifier |
| username | VARCHAR(255) | NOT NULL, UNIQUE | User's login name |
| password | VARCHAR(255) | NOT NULL | BCrypt hashed password |

**Indexes:**
- Primary Key: `id`
- Unique Index: `username`

**Sample Data:**
```sql
INSERT INTO users (username, password) VALUES
('admin', '$2a$10$encrypted_password_hash');
```

**Notes:**
- Passwords are hashed using BCrypt with cost factor 10
- Username is case-sensitive
- No email field (intentionally simple for demo)

---

### 2. courses

Stores course information.

```sql
CREATE TABLE courses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    description VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB;
```

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique course identifier |
| title | VARCHAR(255) | NULL | Course title |
| description | VARCHAR(255) | NULL | Course description |

**Indexes:**
- Primary Key: `id`

**Sample Data:**
```sql
INSERT INTO courses (title, description) VALUES
('Cybersecurity 101', 'Introduction to Web Security');
```

**Notes:**
- Title and description are nullable (can be improved)
- No instructor or enrollment tracking (simplified for demo)

---

### 3. file_metadata

Stores metadata for uploaded files.

```sql
CREATE TABLE file_metadata (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT,
    size BIGINT NOT NULL,
    content_type VARCHAR(255),
    original_filename VARCHAR(255),
    stored_filename VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT FKfevhrjxmm1htuddh9gj4fodpj 
        FOREIGN KEY (course_id) 
        REFERENCES courses (id)
) ENGINE=InnoDB;
```

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique file identifier |
| course_id | BIGINT | FOREIGN KEY → courses(id) | Associated course |
| size | BIGINT | NOT NULL | File size in bytes |
| content_type | VARCHAR(255) | NULL | MIME type (e.g., image/png) |
| original_filename | VARCHAR(255) | NULL | User's original filename |
| stored_filename | VARCHAR(255) | NULL | Server-side filename (with UUID) |

**Indexes:**
- Primary Key: `id`
- Foreign Key: `course_id` → `courses(id)`

**Sample Data:**
```sql
INSERT INTO file_metadata (course_id, size, content_type, original_filename, stored_filename) VALUES
(1, 245678, 'image/png', 'screenshot.png', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890_screenshot.png');
```

**Notes:**
- Actual file content stored in filesystem (`uploads/` directory)
- `stored_filename` includes UUID prefix when security is ON
- No file deletion cascade (files persist even if course deleted)

---

### 4. comment

Stores user comments on courses.

```sql
CREATE TABLE comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_id BIGINT,
    created_at DATETIME(6),
    author VARCHAR(255),
    content TEXT,
    PRIMARY KEY (id),
    CONSTRAINT FKdsub2q6m6519rpas8b075fr7m 
        FOREIGN KEY (course_id) 
        REFERENCES courses (id)
) ENGINE=InnoDB;
```

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique comment identifier |
| course_id | BIGINT | FOREIGN KEY → courses(id) | Associated course |
| created_at | DATETIME(6) | NULL | Timestamp of comment creation |
| author | VARCHAR(255) | NULL | Username of comment author |
| content | TEXT | NULL | Comment text (may contain HTML) |

**Indexes:**
- Primary Key: `id`
- Foreign Key: `course_id` → `courses(id)`

**Sample Data:**
```sql
INSERT INTO comment (course_id, author, content, created_at) VALUES
(1, 'admin', 'Great course!', '2025-11-19 23:45:00');
```

**Notes:**
- `content` is TEXT type to allow longer comments
- When XSS protection is OFF, content may contain raw HTML
- When XSS protection is ON, content is HTML-encoded
- `author` is stored as string (not FK to users table)

---

## Relationships

### courses ← file_metadata (One-to-Many)
- One course can have multiple files
- Cascade: No cascade delete (orphaned files remain)
- Join: `courses.id = file_metadata.course_id`

### courses ← comment (One-to-Many)
- One course can have multiple comments
- Cascade: No cascade delete (orphaned comments remain)
- Join: `courses.id = comment.course_id`

---

## Hibernate DDL Configuration

### Development Mode

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

- **Behavior**: Drops and recreates schema on each application restart
- **Use Case**: Development and testing
- **Data Persistence**: NO (data lost on restart)

### Production Mode

```properties
spring.jpa.hibernate.ddl-auto=validate
```

- **Behavior**: Only validates schema, doesn't modify
- **Use Case**: Production
- **Data Persistence**: YES
- **Migration**: Use Flyway or Liquibase

### Update Mode

```properties
spring.jpa.hibernate.ddl-auto=update
```

- **Behavior**: Updates schema without dropping tables
- **Use Case**: Development after initial setup
- **Data Persistence**: YES
- **Warning**: May not handle all schema changes correctly

---

## Indexes and Performance

### Current Indexes

1. **users.username** (UNIQUE): Fast user lookup during login
2. **Primary Keys**: All tables have auto-increment primary keys
3. **Foreign Keys**: Indexed automatically by InnoDB

### Recommended Additional Indexes (Production)

```sql
-- Index for comment queries by course
CREATE INDEX idx_comment_course_id ON comment(course_id);

-- Index for file queries by course
CREATE INDEX idx_file_metadata_course_id ON file_metadata(course_id);

-- Index for comment sorting by date
CREATE INDEX idx_comment_created_at ON comment(created_at DESC);
```

---

## Data Types and Constraints

### String Lengths

| Field | Max Length | Rationale |
|-------|-----------|-----------|
| username | 255 | Standard VARCHAR limit |
| password | 255 | BCrypt hash is 60 chars, 255 for future-proofing |
| course title | 255 | Reasonable course title length |
| course description | 255 | Short description only |
| filename | 255 | Filesystem path limit |
| comment author | 255 | Matches username length |
| comment content | TEXT (65,535) | Allows long comments |

### Numeric Types

- **BIGINT**: Used for all IDs (supports up to 9,223,372,036,854,775,807)
- **BIGINT**: Used for file size (supports files up to 8 exabytes)

### Date/Time Types

- **DATETIME(6)**: Microsecond precision for timestamps

---

## Migration Notes

### From H2 to MySQL

The application was migrated from H2 (in-memory) to MySQL (persistent). Key changes:

1. **Dialect**: Changed from H2Dialect to MySQLDialect
2. **Driver**: Changed from H2 to MySQL Connector/J
3. **DDL Strategy**: Changed from `create-drop` to `update` (then back to `create-drop` for clean setup)
4. **Schema Recreation**: Dropped all tables to fix email field issue

### Schema Evolution

**Version 1.0** (Initial):
- Basic schema with users, courses, files, comments
- No email field in users table
- No soft delete support
- No audit fields (created_by, updated_at)

**Future Enhancements**:
- Add `created_at`, `updated_at` to all tables
- Add `deleted_at` for soft deletes
- Add `email` to users table
- Add `role` to users table (ADMIN, USER, TEACHER)
- Add `enrollment` table for user-course relationships

---

## Database Initialization

### DataSeeder

On application startup, `DataSeeder` runs:

```java
@Bean
public CommandLineRunner initData(CourseRepository courseRepository, AuthService authService) {
    return args -> {
        // Create default course if none exist
        if (courseRepository.count() == 0) {
            Course course = new Course();
            course.setTitle("Cybersecurity 101");
            course.setDescription("Introduction to Web Security");
            courseRepository.save(course);
        }
        
        // Create default admin user
        try {
            authService.signup("admin", "password");
        } catch (Exception e) {
            // User might already exist
        }
    };
}
```

---

## Backup and Restore

### Backup

```bash
# Full database backup
mysqldump -u root -p securecourse > securecourse_backup.sql

# Specific table backup
mysqldump -u root -p securecourse users > users_backup.sql
```

### Restore

```bash
# Restore full database
mysql -u root -p securecourse < securecourse_backup.sql

# Restore specific table
mysql -u root -p securecourse < users_backup.sql
```

---

## Query Examples

### Get all courses with file count

```sql
SELECT 
    c.id,
    c.title,
    COUNT(f.id) as file_count
FROM courses c
LEFT JOIN file_metadata f ON c.id = f.course_id
GROUP BY c.id, c.title;
```

### Get all comments for a course with author

```sql
SELECT 
    cm.id,
    cm.content,
    cm.author,
    cm.created_at
FROM comment cm
WHERE cm.course_id = 1
ORDER BY cm.created_at DESC;
```

### Find users who commented on a specific course

```sql
SELECT DISTINCT cm.author
FROM comment cm
WHERE cm.course_id = 1;
```

---

## Storage Considerations

### Database Size Estimates

| Table | Rows (Est.) | Size per Row | Total Size |
|-------|-------------|--------------|------------|
| users | 1,000 | ~500 bytes | ~500 KB |
| courses | 100 | ~300 bytes | ~30 KB |
| file_metadata | 10,000 | ~400 bytes | ~4 MB |
| comment | 50,000 | ~500 bytes | ~25 MB |

**Total Database Size**: ~30 MB (excluding file content)

### File Storage

Files are stored in `uploads/` directory:
- Average file size: 500 KB
- 10,000 files: ~5 GB

**Total Storage**: Database (30 MB) + Files (5 GB) = ~5 GB

---

## Security Considerations

1. **Password Storage**: BCrypt hashed, never plaintext
2. **SQL Injection**: Prevented by JPA/PreparedStatements (when toggle ON)
3. **XSS**: Content encoding (when toggle ON)
4. **File Storage**: Separate from database, UUID filenames (when toggle ON)
5. **Access Control**: No row-level security implemented (future enhancement)

---

## Monitoring Queries

### Check table sizes

```sql
SELECT 
    table_name AS 'Table',
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'securecourse'
ORDER BY (data_length + index_length) DESC;
```

### Check row counts

```sql
SELECT 
    table_name,
    table_rows
FROM information_schema.tables
WHERE table_schema = 'securecourse';
```
