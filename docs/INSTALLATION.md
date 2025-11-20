# Installation Guide

## Prerequisites

### Required Software

| Software | Minimum Version | Download Link |
|----------|----------------|---------------|
| Java JDK | 17+ | https://adoptium.net/ |
| Node.js | 18+ | https://nodejs.org/ |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/ |
| Maven | 3.9+ | https://maven.apache.org/ (or use included wrapper) |

### System Requirements

- **OS**: Windows 10+, macOS 10.15+, or Linux
- **RAM**: 4GB minimum, 8GB recommended
- **Disk Space**: 2GB free space
- **Network**: Internet connection for dependency downloads

---

## MySQL Setup

### 1. Install MySQL

**macOS (Homebrew):**
```bash
brew install mysql
brew services start mysql
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
```

**Windows:**
- Download MySQL Installer from official website
- Run installer and follow setup wizard
- Start MySQL service

### 2. Create Database

```bash
mysql -u root -p
```

```sql
CREATE DATABASE securecourse;
EXIT;
```

### 3. Configure MySQL User (Optional)

If using a different user than `root`:

```sql
CREATE USER 'securecourse_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON securecourse.* TO 'securecourse_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## Backend Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd secureCourse/backend
```

### 2. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/securecourse?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

**Important Configuration Options:**

- `spring.jpa.hibernate.ddl-auto`:
  - `create-drop`: Recreates schema on each startup (development)
  - `update`: Updates schema without dropping (recommended after initial setup)
  - `validate`: Only validates schema (production)

### 3. Install Dependencies

Using Maven wrapper (recommended):
```bash
./mvnw clean install
```

Or using system Maven:
```bash
mvn clean install
```

### 4. Run Backend

```bash
./mvnw spring-boot:run
```

**Expected Output:**
```
Started BackendApplication in 2.5 seconds (process running for 2.8)
Tomcat started on port 8080 (http) with context path '/'
```

### 5. Verify Backend

Test the health endpoint:
```bash
curl http://localhost:8080/toggles
```

Expected response:
```json
{
  "sqlInjectionProtection": true,
  "fileUploadSecurity": true,
  "xssProtection": true
}
```

---

## Frontend Setup

### 1. Navigate to Frontend Directory

```bash
cd ../frontend
```

### 2. Install Dependencies

```bash
npm install
```

**Expected Output:**
```
added 289 packages in 12s
```

### 3. Configure API Proxy (Already Configured)

The `vite.config.js` is pre-configured to proxy API requests to backend:

```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/auth': 'http://localhost:8080',
      '/course': 'http://localhost:8080',
      '/toggles': 'http://localhost:8080',
      '/files': 'http://localhost:8080'
    }
  }
})
```

### 4. Run Frontend

```bash
npm run dev
```

**Expected Output:**
```
VITE v5.0.0  ready in 811 ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

### 5. Verify Frontend

Open browser and navigate to:
```
http://localhost:5173
```

You should see the SecureCourse login page.

---

## Environment Variables

### Backend Environment Variables

Create `.env` file in `backend/` directory (optional):

```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/securecourse
DB_USERNAME=root
DB_PASSWORD=your_password

# Server
SERVER_PORT=8080

# Security
JWT_SECRET=your_secret_key_here  # If implementing JWT
```

**Note**: Current implementation uses `application.properties`. For production, use environment variables or Spring profiles.

### Frontend Environment Variables

Create `.env` file in `frontend/` directory (optional):

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=SecureCourse
```

---

## Running Both Services

### Option 1: Separate Terminals

**Terminal 1 (Backend):**
```bash
cd backend
./mvnw spring-boot:run
```

**Terminal 2 (Frontend):**
```bash
cd frontend
npm run dev
```

### Option 2: Using tmux (Linux/macOS)

```bash
# Create new tmux session
tmux new -s securecourse

# Split window horizontally
Ctrl+B then "

# In first pane
cd backend && ./mvnw spring-boot:run

# Switch to second pane (Ctrl+B then arrow key)
cd frontend && npm run dev
```

### Option 3: Using Screen (Linux/macOS)

```bash
# Start backend in background
screen -dmS backend bash -c 'cd backend && ./mvnw spring-boot:run'

# Start frontend in background
screen -dmS frontend bash -c 'cd frontend && npm run dev'

# List running screens
screen -ls

# Attach to backend
screen -r backend

# Detach: Ctrl+A then D
```

---

## Default Data

On first startup, the application automatically seeds:

### Default User
- **Username**: `admin`
- **Password**: `password`

### Default Course
- **Title**: "Cybersecurity 101"
- **Description**: "Introduction to Web Security"

---

## Troubleshooting

### Backend Issues

**Issue**: `Port 8080 already in use`

**Solution**:
```bash
# Find process using port 8080
lsof -ti:8080

# Kill the process
kill -9 $(lsof -ti:8080)
```

**Issue**: `Access denied for user 'root'@'localhost'`

**Solution**:
- Verify MySQL is running: `mysql.server status`
- Check password in `application.properties`
- Reset MySQL root password if needed

**Issue**: `Table 'securecourse.users' doesn't exist`

**Solution**:
- Ensure `spring.jpa.hibernate.ddl-auto=create-drop` or `update`
- Drop and recreate database:
```sql
DROP DATABASE securecourse;
CREATE DATABASE securecourse;
```

### Frontend Issues

**Issue**: `npm: command not found`

**Solution**:
- Install Node.js from https://nodejs.org/
- Verify installation: `node --version`

**Issue**: `ECONNREFUSED 127.0.0.1:8080`

**Solution**:
- Ensure backend is running
- Check backend logs for errors
- Verify proxy configuration in `vite.config.js`

**Issue**: `Port 5173 is in use`

**Solution**:
```bash
# Kill process on port 5173
lsof -ti:5173 | xargs kill -9

# Or use different port
npm run dev -- --port 5174
```

### Database Issues

**Issue**: `Communications link failure`

**Solution**:
- Start MySQL service
- Verify MySQL is listening on port 3306
- Check firewall settings

**Issue**: `Unknown database 'securecourse'`

**Solution**:
```bash
mysql -u root -p -e "CREATE DATABASE securecourse;"
```

---

## Verification Checklist

After installation, verify:

- [ ] MySQL is running and accessible
- [ ] Database `securecourse` exists
- [ ] Backend starts without errors on port 8080
- [ ] Frontend starts without errors on port 5173
- [ ] Can access http://localhost:5173 in browser
- [ ] Can login with admin/password
- [ ] Security toggles are visible and functional
- [ ] Can upload images to course
- [ ] Can post comments

---

## Next Steps

1. Read [SECURITY.md](./SECURITY.md) to understand vulnerability demos
2. Review [API_DOCS.md](./API_DOCS.md) for API reference
3. Check [DEPLOYMENT.md](./DEPLOYMENT.md) for production deployment
4. Explore [ARCHITECTURE.md](./ARCHITECTURE.md) for system design

---

## Development Mode

### Hot Reload

- **Backend**: Spring Boot DevTools enables automatic restart
- **Frontend**: Vite HMR (Hot Module Replacement) for instant updates

### Debugging

**Backend (IntelliJ IDEA):**
1. Open `backend` folder as Maven project
2. Set breakpoints in code
3. Run → Debug 'BackendApplication'

**Frontend (VS Code):**
1. Install "Debugger for Chrome" extension
2. Set breakpoints in `.jsx` files
3. Press F5 to start debugging

### Logging

**Backend Logs:**
- Location: Console output
- Level: INFO (configurable in `application.properties`)
- SQL Queries: Enabled via `spring.jpa.show-sql=true`

**Frontend Logs:**
- Location: Browser console (F12)
- Use `console.log()` for debugging

---

## Clean Installation

To start fresh:

```bash
# Drop database
mysql -u root -p -e "DROP DATABASE securecourse; CREATE DATABASE securecourse;"

# Clean backend
cd backend
./mvnw clean
rm -rf target/

# Clean frontend
cd ../frontend
rm -rf node_modules/
rm -rf dist/
npm install

# Restart both services
```
