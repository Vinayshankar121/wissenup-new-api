# WissenUp SaaS Platform - Build & Run Guide

**Version**: 1.0 - Production Ready  
**Date**: August 18, 2026  
**Architecture**: Monolithic Spring Boot + React Frontend

---

## 📁 PROJECT STRUCTURE (CLEANED)

### Backend (WissenUp-api)
```
WissenUp-api/
├── src/main/
│   ├── java/com/wissenup/
│   │   ├── domain/
│   │   │   ├── identity/     ✅ Phase 1: Authentication & RBAC
│   │   │   ├── academic/     ✅ Phase 1: Academic Foundation
│   │   │   ├── platform/     ✅ Phase 2: SaaS Platform Layer
│   │   │   └── shared/       ✅ Shared entities & security
│   │   └── config/           ✅ Spring configuration
│   └── resources/
│       ├── db/migration/     ✅ Flyway migrations (V1, V2, V3)
│       └── application.yml   ✅ Configuration
├── pom.xml                   ✅ Maven configuration
└── target/                   (Generated - build output)

Legacy/Archive Folders (Can be removed):
├── academic/                 ❌ Separate module - not used
├── AttendanceService/        ❌ Separate module - not used
├── ExamService/              ❌ Separate module - not used
├── FeeService/               ❌ Separate module - not used
├── IdentityService/          ❌ Separate module - not used
├── Organization/             ❌ Separate module - not used
├── PlatformService/          ❌ Separate module - not used
├── StaffService/             ❌ Separate module - not used
├── StudentService/           ❌ Separate module - not used
├── TimetableService/         ❌ Separate module - not used
├── common-security/          ❌ Legacy - integrated into src/
└── frontend/                 ❌ Legacy - use WissenUp-ui instead
```

### Frontend (WissenUp-ui)
```
WissenUp-ui/
├── src/
│   ├── components/          ✅ Reusable UI components
│   ├── pages/
│   │   ├── superadmin/      ✅ Super Admin Portal (NEW)
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Schools.tsx
│   │   │   ├── Onboarding.tsx
│   │   │   └── AuditLogs.tsx
│   │   ├── org/             ✅ Organization pages
│   │   └── ...              ✅ Other pages
│   ├── services/
│   │   ├── platformService.ts ✅ NEW - Platform API client
│   │   ├── authService.js     ✅ Authentication
│   │   └── ...                ✅ Other services
│   ├── stores/              ✅ Zustand state management
│   ├── routes/              ✅ React Router configuration
│   └── ...
├── package.json             ✅ Node dependencies
├── vite.config.js           ✅ Vite build configuration
└── .env                      ✅ Environment variables
```

---

## 🧹 CLEANUP STEPS

### Step 1: Archive Legacy Service Modules (Optional)

If you want to clean up the project folder:

```bash
# Navigate to WissenUp-api
cd C:\Users\vgraddagunta\Desktop\VSCODE\sc er\WissenUp-api

# Create an archive folder for legacy services
mkdir ../_archive_legacy_services

# Move unused service modules (do NOT delete - archive them)
move academic ..\_archive_legacy_services\
move AttendanceService ..\_archive_legacy_services\
move ExamService ..\_archive_legacy_services\
move FeeService ..\_archive_legacy_services\
move IdentityService ..\_archive_legacy_services\
move Organization ..\_archive_legacy_services\
move PlatformService ..\_archive_legacy_services\
move StaffService ..\_archive_legacy_services\
move StudentService ..\_archive_legacy_services\
move TimetableService ..\_archive_legacy_services\
move common-security ..\_archive_legacy_services\
move frontend ..\_archive_legacy_services\
```

### Step 2: Verify Core Project Structure

After cleanup, verify the core project exists:

```
WissenUp-api/
├── src/                    ✅ Main Spring Boot application
├── pom.xml                 ✅ Maven POM
├── target/                 (Will be created during build)
└── WissenUp-ui/            ✅ React frontend (symlink or separate folder)
```

---

## 🏗️ BUILD INSTRUCTIONS

### Backend Build (Spring Boot)

#### Prerequisites
- Java 17+ installed
- Maven 3.8+ installed
- PostgreSQL database running

#### Build Steps

**1. Navigate to backend directory:**
```bash
cd "C:\Users\vgraddagunta\Desktop\VSCODE\sc er\WissenUp-api"
```

**2. Clean and compile:**
```bash
mvn clean compile
```

**3. Run tests (optional):**
```bash
mvn test
```

**4. Package as JAR:**
```bash
mvn clean package -DskipTests
```

**Build Output:**
```
target/wissenup-backend-1.0.0-SNAPSHOT.jar
```

### Frontend Build (React + Vite)

#### Prerequisites
- Node.js 18+ installed
- npm 9+ installed

#### Build Steps

**1. Navigate to frontend directory:**
```bash
cd "C:\Users\vgraddagunta\Desktop\VSCODE\sc er\WissenUp-ui"
```

**2. Install dependencies:**
```bash
npm install
```

**3. Build production bundle:**
```bash
npm run build
```

**Build Output:**
```
dist/                      # Production-ready static files
├── index.html
├── assets/
│   ├── *.js
│   ├── *.css
│   └── ...
└── ...
```

---

## 🚀 RUN INSTRUCTIONS

### Option 1: Run in Development Mode (Recommended for Development)

#### Backend - Development Mode

**1. Start the application:**
```bash
cd "C:\Users\vgraddagunta\Desktop\VSCODE\sc er\WissenUp-api"
mvn spring-boot:run
```

**2. Application starts on:**
```
Server: http://localhost:8080
API: http://localhost:8080/api/v1
Swagger UI: http://localhost:8080/swagger-ui.html
```

#### Frontend - Development Mode

**1. In a new terminal, start the dev server:**
```bash
cd "C:\Users\vgraddagunta\Desktop\VSCODE\sc er\WissenUp-ui"
npm run dev
```

**2. Frontend starts on:**
```
http://localhost:5173
```

**3. Access the application:**
- Open browser to `http://localhost:5173`
- Login with credentials
- Navigate to `/superadmin/dashboard` for platform portal

---

### Option 2: Run Production Build

#### Backend - Production Mode

**1. Build JAR:**
```bash
cd "C:\Users\vgraddagunta\Desktop\VSCODE\sc er\WissenUp-api"
mvn clean package -DskipTests
```

**2. Run JAR:**
```bash
java -jar target/wissenup-backend-1.0.0-SNAPSHOT.jar
```

**3. Application starts on:**
```
http://localhost:8080
```

#### Frontend - Production Mode

**1. Build static files:**
```bash
cd "C:\Users\vgraddagunta\Desktop\VSCODE\sc er\WissenUp-ui"
npm run build
```

**2. Serve production build (using npm serve):**
```bash
npm install -g serve
serve -s dist -l 5173
```

**3. Access at:**
```
http://localhost:5173
```

---

## 🔧 ENVIRONMENT CONFIGURATION

### Backend Configuration

**File:** `WissenUp-api/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wissenup
    username: postgres
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true

server:
  port: 8080
  servlet:
    context-path: /api/v1

jwt:
  secret: your-secret-key-min-32-chars-long
  expiration: 86400000

auth:
  token-storage: sessionStorage
```

### Frontend Configuration

**File:** `WissenUp-ui/.env`

```env
VITE_API_URL=http://localhost:8080/api/v1
VITE_IDENTITY_SERVICE_URL=http://localhost:8080/api/v1
VITE_PLATFORM_SERVICE_URL=http://localhost:8080/api/v1
VITE_ORGANIZATION_SERVICE_URL=http://localhost:8080/api/v1
```

---

## 📦 DOCKER DEPLOYMENT (Optional)

### Backend Docker

**Create `Dockerfile` in WissenUp-api:**
```dockerfile
FROM maven:3.8-openjdk-17 AS builder
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and run:**
```bash
docker build -t wissenup-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/wissenup \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=password \
  wissenup-backend
```

### Frontend Docker

**Create `Dockerfile` in WissenUp-ui:**
```dockerfile
FROM node:18-alpine AS builder
WORKDIR /build
COPY package*.json .
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Build and run:**
```bash
docker build -t wissenup-frontend .
docker run -p 80:80 wissenup-frontend
```

---

## 🧪 TESTING

### Backend Tests

**Run all tests:**
```bash
mvn test
```

**Run specific test class:**
```bash
mvn test -Dtest=OnboardingServiceTest
```

**Run with coverage:**
```bash
mvn test jacoco:report
```

### Frontend Tests

**Run tests:**
```bash
npm run test
```

**Run with coverage:**
```bash
npm run test:coverage
```

---

## 📊 API DOCUMENTATION

### Swagger/OpenAPI

**Access Swagger UI:**
```
http://localhost:8080/swagger-ui.html
```

**OpenAPI JSON:**
```
http://localhost:8080/v3/api-docs
```

---

## 🔍 TROUBLESHOOTING

### Backend Issues

**Issue: Port 8080 already in use**
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

**Issue: Database connection failed**
- Verify PostgreSQL is running
- Check connection string in application.yml
- Verify database exists: `wissenup`

**Issue: Maven build fails**
```bash
# Clear Maven cache
mvn clean

# Reinstall dependencies
mvn dependency:resolve
```

### Frontend Issues

**Issue: Node modules not working**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Issue: Port 5173 in use**
```bash
npm run dev -- --port 3000
```

---

## ✅ VERIFICATION CHECKLIST

### Backend Verification
- [ ] Backend compiles without errors: `mvn clean compile`
- [ ] Database migrations run: Check Flyway in console logs
- [ ] Application starts on port 8080
- [ ] Swagger UI accessible at `/swagger-ui.html`
- [ ] Health check: `curl http://localhost:8080/actuator/health`

### Frontend Verification
- [ ] Frontend dependencies install: `npm install` completes
- [ ] Dev server starts: `npm run dev`
- [ ] Frontend accessible at `http://localhost:5173`
- [ ] Production build successful: `npm run build`
- [ ] No console errors in browser

### Integration Verification
- [ ] Both backend and frontend running
- [ ] Login page loads
- [ ] Can login with valid credentials
- [ ] Dashboard metrics load
- [ ] API calls return valid data
- [ ] Super admin portal pages work

---

## 📈 PERFORMANCE TIPS

### Backend
- Use `mvn clean compile` instead of `mvn install` for faster builds
- Run tests separately: `mvn package -DskipTests`
- Use Maven profiler: `mvn -DperformanceProfile=true spring-boot:run`

### Frontend
- Use `npm ci` instead of `npm install` in CI/CD
- Use `npm run build` with `--sourcemap=false` for production
- Enable compression in nginx/server

---

## 🚢 DEPLOYMENT CHECKLIST

### Pre-Deployment
- [ ] All tests passing
- [ ] No console warnings
- [ ] Environment variables configured
- [ ] Database backups created
- [ ] API keys and secrets configured

### Deployment
- [ ] Backend JAR built and tested
- [ ] Frontend build created and tested
- [ ] Database migrations reviewed
- [ ] Nginx/reverse proxy configured
- [ ] SSL certificates configured

### Post-Deployment
- [ ] Smoke tests passed
- [ ] Monitoring configured
- [ ] Logging configured
- [ ] Alerts configured
- [ ] Health checks passing

---

## 📚 QUICK REFERENCE

### Essential Commands

**Backend:**
```bash
# Compile
mvn clean compile

# Run in development
mvn spring-boot:run

# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/wissenup-backend-1.0.0-SNAPSHOT.jar

# Run tests
mvn test
```

**Frontend:**
```bash
# Install dependencies
npm install

# Start dev server
npm run dev

# Build production
npm run build

# Preview production build
npm run preview

# Run tests
npm run test
```

---

## 📞 SUPPORT

For issues or questions:
1. Check the logs in console
2. Review error messages
3. Check database connectivity
4. Verify environment configuration
5. Check API documentation at `/swagger-ui.html`

---

**You're all set!** 🎉 

The WissenUp SaaS platform is ready to build and run. Start with development mode, then move to production when ready.
