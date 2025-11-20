# Deployment Guide

## Production Deployment

**⚠️ CRITICAL WARNING**: This application contains intentional security vulnerabilities for educational purposes. **DO NOT deploy to production** or expose to the internet without removing all vulnerable code paths.

This guide is provided for educational purposes to demonstrate deployment concepts.

---

## Pre-Deployment Checklist

Before deploying, ensure:

- [ ] All security toggles are permanently set to ON
- [ ] Vulnerable code paths are removed
- [ ] CSRF protection is enabled
- [ ] HTTPS is configured
- [ ] Database credentials are secured
- [ ] File upload directory has proper permissions
- [ ] Error messages don't leak sensitive information
- [ ] Logging is configured for production
- [ ] Rate limiting is implemented
- [ ] Security headers are added

---

## Building for Production

### Backend Build

```bash
cd backend

# Update application.properties for production
# Change ddl-auto from create-drop to validate
# Update database credentials

# Build JAR file
./mvnw clean package -DskipTests

# JAR file location
ls target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend Build

```bash
cd frontend

# Build production bundle
npm run build

# Build output location
ls dist/
```

---

## Deployment Option 1: Standalone Services

### Backend Deployment

**1. Copy JAR to server:**
```bash
scp target/backend-0.0.1-SNAPSHOT.jar user@server:/opt/securecourse/
```

**2. Create systemd service:**

Create `/etc/systemd/system/securecourse-backend.service`:

```ini
[Unit]
Description=SecureCourse Backend
After=mysql.service

[Service]
Type=simple
User=securecourse
WorkingDirectory=/opt/securecourse
ExecStart=/usr/bin/java -jar backend-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/securecourse"
Environment="SPRING_DATASOURCE_USERNAME=securecourse_user"
Environment="SPRING_DATASOURCE_PASSWORD=secure_password"

[Install]
WantedBy=multi-user.target
```

**3. Start service:**
```bash
sudo systemctl daemon-reload
sudo systemctl enable securecourse-backend
sudo systemctl start securecourse-backend
sudo systemctl status securecourse-backend
```

### Frontend Deployment (Nginx)

**1. Copy build files:**
```bash
scp -r dist/* user@server:/var/www/securecourse/
```

**2. Configure Nginx:**

Create `/etc/nginx/sites-available/securecourse`:

```nginx
server {
    listen 80;
    server_name securecourse.example.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name securecourse.example.com;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/securecourse.example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/securecourse.example.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;

    # Frontend
    root /var/www/securecourse;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Backend API Proxy
    location /auth {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /course {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /toggles {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # File uploads
    client_max_body_size 10M;
}
```

**3. Enable site:**
```bash
sudo ln -s /etc/nginx/sites-available/securecourse /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## Deployment Option 2: Embedded Frontend

Serve React build from Spring Boot.

### 1. Build Frontend

```bash
cd frontend
npm run build
```

### 2. Copy Build to Spring Boot

```bash
# Create static directory in Spring Boot
mkdir -p backend/src/main/resources/static

# Copy frontend build
cp -r frontend/dist/* backend/src/main/resources/static/
```

### 3. Update Spring Boot Configuration

Add to `application.properties`:

```properties
# Serve static content
spring.web.resources.static-locations=classpath:/static/
spring.mvc.static-path-pattern=/**
```

### 4. Build Combined JAR

```bash
cd backend
./mvnw clean package
```

### 5. Deploy Single JAR

```bash
java -jar backend-0.0.1-SNAPSHOT.jar
```

Access application at: `http://localhost:8080`

---

## Deployment Option 3: Docker

### Dockerfile (Backend)

Create `backend/Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Dockerfile (Frontend)

Create `frontend/Dockerfile`:

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: securecourse
      MYSQL_USER: securecourse_user
      MYSQL_PASSWORD: secure_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - securecourse-network

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/securecourse
      SPRING_DATASOURCE_USERNAME: securecourse_user
      SPRING_DATASOURCE_PASSWORD: secure_password
    depends_on:
      - mysql
    networks:
      - securecourse-network

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - securecourse-network

volumes:
  mysql_data:

networks:
  securecourse-network:
    driver: bridge
```

### Deploy with Docker Compose

```bash
docker-compose up -d
```

---

## Environment Variables (Production)

### Backend

```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/securecourse
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=strong_password_here

# JPA
export SPRING_JPA_HIBERNATE_DDL_AUTO=validate
export SPRING_JPA_SHOW_SQL=false

# Server
export SERVER_PORT=8080

# Security
export SPRING_SECURITY_CSRF_ENABLED=true

# File Upload
export FILE_UPLOAD_DIR=/var/securecourse/uploads
```

### Frontend

Update `vite.config.js` for production API URL:

```javascript
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/auth': process.env.VITE_API_URL || 'http://localhost:8080',
      '/course': process.env.VITE_API_URL || 'http://localhost:8080',
      '/toggles': process.env.VITE_API_URL || 'http://localhost:8080',
    }
  }
})
```

---

## Database Migration

For production, use Flyway or Liquibase for schema management.

### Flyway Setup

**1. Add dependency to `pom.xml`:**

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

**2. Create migration scripts:**

`src/main/resources/db/migration/V1__Initial_Schema.sql`:

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    description VARCHAR(255)
);

-- Add other tables...
```

**3. Configure in `application.properties`:**

```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

## Monitoring & Logging

### Application Logging

Configure `logback-spring.xml`:

```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/securecourse/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/securecourse/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### Health Checks

Add Spring Boot Actuator:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Configure endpoints:

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

---

## Performance Optimization

### Backend

1. **Enable caching:**
```properties
spring.cache.type=caffeine
```

2. **Connection pooling:**
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

3. **JVM tuning:**
```bash
java -Xms512m -Xmx2g -jar app.jar
```

### Frontend

1. **Enable gzip compression** (Nginx):
```nginx
gzip on;
gzip_types text/plain text/css application/json application/javascript;
```

2. **Browser caching:**
```nginx
location ~* \.(js|css|png|jpg|jpeg|gif|ico)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

---

## Backup Strategy

### Database Backup

```bash
# Daily backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u root -p securecourse > /backups/securecourse_$DATE.sql
find /backups -name "securecourse_*.sql" -mtime +30 -delete
```

### File Backup

```bash
# Backup uploaded files
tar -czf /backups/uploads_$DATE.tar.gz /var/securecourse/uploads/
```

---

## SSL/TLS Configuration

### Let's Encrypt (Certbot)

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d securecourse.example.com

# Auto-renewal
sudo certbot renew --dry-run
```

---

## Rollback Procedure

1. **Stop services:**
```bash
sudo systemctl stop securecourse-backend
sudo systemctl stop nginx
```

2. **Restore database:**
```bash
mysql -u root -p securecourse < /backups/securecourse_backup.sql
```

3. **Deploy previous version:**
```bash
cp /backups/backend-previous.jar /opt/securecourse/backend.jar
```

4. **Restart services:**
```bash
sudo systemctl start securecourse-backend
sudo systemctl start nginx
```

---

## Security Hardening

1. **Disable vulnerable endpoints** in production
2. **Remove toggle functionality** entirely
3. **Enable CSRF protection**
4. **Implement rate limiting**
5. **Add WAF (Web Application Firewall)**
6. **Regular security audits**
7. **Keep dependencies updated**

---

## Recommended Server Configuration

- **CPU**: 2+ cores
- **RAM**: 4GB minimum
- **Disk**: 20GB SSD
- **OS**: Ubuntu 22.04 LTS or similar
- **Firewall**: UFW or iptables configured
- **Reverse Proxy**: Nginx or Apache
- **SSL**: Let's Encrypt certificates
