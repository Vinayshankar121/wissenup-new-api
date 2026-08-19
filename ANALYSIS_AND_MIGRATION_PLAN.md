# WissenUp Backend - Analysis & Migration Plan

**Document Date:** 2026-08-18  
**Current Status:** 11 separate Spring Boot services  
**Target Status:** 1 modular Spring Boot monolith  
**Scope:** 465 Java files across 11 services  

---

## PART A: CURRENT STATE ANALYSIS

### A.1. SERVICE INVENTORY

| Service | Port | Key Responsibility | Java Files | Package Structure |
|---------|------|-------------------|-----------|-------------------|
| **IdentityService** | 1113 | JWT auth, OTP verification, user login | ~40 | controller, service, entity, repository, dto, exception |
| **PlatformService** | 1111 | Modules, roles, permissions, subscription plans | ~35 | api, service, entity, repository |
| **Organization** | 1112 | Organization setup, settings, subscriptions | ~25 | controller, service, entity, repository, dto |
| **Academic** | 1114 | Academic years, classes, sections, subjects | ~30 | controller, service, entity, repository, dto |
| **StudentService** | 1115 | Students, parents, enrollments | ~45 | controller, service, entity, repository, dto, mapper |
| **StaffService** | 1116 | Staff, departments, designations, assignments | ~35 | controller, service, entity, repository, dto |
| **AttendanceService** | 1117 | Attendance tracking, sessions | ~20 | controller, service, entity, repository |
| **FeeService** | 1118 | Fee structures, types, payments, student fees | ~40 | controller, service, entity, repository, dto |
| **ExamService** | 1119 | Exams, timetables, marks, results | ~40 | controller, service, entity, repository, dto |
| **TimetableService** | 1120 | Class timetables, periods, substitutions | ~25 | controller, service, entity, repository, dto |
| **common-security** | — | JWT, security config, tenant isolation | ~30 | JwtService, JwtAuthenticationFilter, TenantRepositoryAspect |
| **TOTAL** | | | **465** | |

### A.2. CURRENT PACKAGE STRUCTURE

Each service follows a consistent pattern:
```
{ServiceName}Service/
├── src/main/java/com/wissenup/{ServiceName}/
│   ├── {ServiceName}Application.java       (Spring Boot main)
│   ├── controller/                         (REST endpoints)
│   ├── service/ (+ impl/)                  (Business logic)
│   ├── repository/                         (JPA interfaces)
│   ├── entity/                             (JPA entities)
│   ├── dto/request/ + response/            (API contracts)
│   ├── mapper/                             (Entity <-> DTO mapping)
│   ├── exception/                          (Custom exceptions + GlobalExceptionHandler)
│   └── config/                             (Service-specific config)
├── src/test/java/
├── pom.xml
└── application.properties                   (Hardcoded, needs externalization)
```

### A.3. CRITICAL OBSERVATIONS

#### ✅ Strengths:
1. **Clean separation:** Each domain has its own package (controller, service, repository, entity, dto, mapper)
2. **Consistent patterns:** All services follow the same structure
3. **Security foundation:** common-security module provides JWT, CORS, multi-tenant interceptors, AOP-based repository filtering
4. **Multi-tenancy awareness:** 
   - `organization_id` field on all tenant-scoped entities
   - JwtClaims carries organizationId from JWT
   - SecurityContextUtil.requireOrganizationAccess() validates tenant
   - TenantRepositoryAspect filters results at AOP level
   - TenantRequestBodyAdvice injects organizationId into payloads
5. **Database design:** 30+ tables with proper foreign keys, audit fields, and soft-deletes
6. **Flyway migrations:** Clean versioned migrations (V1-V6)

#### ❌ Critical Issues:
1. **Hardcoded Secrets:**
   - Database: `postgres` / `admin123` in application.properties
   - Email: Gmail credentials (vinaynukala65@gmail.com/snmpiiajdfsgwepg) in IdentityService
   - JWT Secret: default value in code fallback
   - Service URLs: hardcoded localhost:1113, 1112, 1111, etc.

2. **No Environment Configuration:**
   - application.properties is checked into git with secrets
   - No application-dev.yml, application-prod.yml, application-test.yml
   - No .env.example for developers

3. **API Issues:**
   - No API versioning (/api/v1/)
   - CORS = "*" in some places, hardcoded localhost:5173 in common-security
   - No pagination on list endpoints (getAllStudents, getAllStaff return all rows)
   - No standardized error response format (each service has its own)

4. **Database Sharing:**
   - All 11 services share the same PostgreSQL database (`wissenup`)
   - Violates microservices principle, but acceptable for monolith conversion
   - No separate schemas per service

5. **Synchronous Inter-service Calls:**
   - StudentService calls IdentityService, PlatformService, Organization service
   - No async messaging (no Kafka, no events)
   - Creates tight coupling through HTTP calls

6. **No Docker:**
   - No Dockerfile for any service
   - No docker-compose for local development

7. **Minimal Testing:**
   - 22 test files total (mostly backend)
   - No tenant isolation tests
   - No frontend tests

8. **No API Documentation:**
   - No OpenAPI/Swagger
   - Postman notes exist for PlatformService only

---

### A.4. SECURITY ANALYSIS

#### Authentication Flow:
```
1. POST /api/auth/login (email, password)
   → IdentityService.AuthService.login()
   → Finds user, validates password
   → Returns LoginResponse with email (OTP not yet sent)

2. POST /api/auth/otp/verify (email, OTP)
   → OtpService.verifyOtp()
   → AuthService.completeLogin()
   → Generates JWT with claims: userId, organizationId, roleId, email

3. All subsequent requests:
   → Authorization: Bearer {jwt}
   → JwtAuthenticationFilter parses & validates token
   → Sets JwtClaims in SecurityContext
   → TenantRepositoryAspect filters results by organizationId
```

#### Tenant Isolation Layers:
1. **JWT Claims:** organizationId embedded in token (only valid for that tenant)
2. **OrganizationTenantInterceptor:** Validates organizationId in URL matches JWT
3. **SecurityContextUtil.requireOrganizationAccess():** Explicit checks in services
4. **TenantRepositoryAspect:** AOP intercepts repository calls, filters by organizationId
5. **TenantRequestBodyAdvice:** Automatically injects organizationId into POST/PUT bodies

#### Security Issues:
- ❌ JWT stored in localStorage (XSS vulnerable) — frontend issue
- ❌ No refresh token (session doesn't expire properly)
- ❌ CORS allows "*" in some places
- ⚠️ No rate limiting on auth endpoints (OTP brute force risk)
- ⚠️ OTP delivered via email only (5 min expiry)
- ❌ No password requirements documented

---

### A.5. DATABASE SCHEMA SUMMARY

**30+ Tables:**
- **Tenancy:** organizations, organization_settings, organization_subscriptions, organization_modules, users, user_roles
- **Academic:** academic_years, classes, sections, subjects, class_subjects
- **Staff:** departments, designations, staff, staff_subject_assignments, staff_class_assignments
- **Students:** students, parents, student_parents, student_enrollments
- **Attendance:** student_attendance_sessions, student_attendance
- **Exams:** exam_types, exams, exam_targets, exam_timetables, student_marks
- **Fees:** fee_types, fee_structures, student_fees, fee_payments
- **Timetable:** timetable_periods, class_timetables, class_timetable_details
- **Platform:** modules, roles, permissions, role_permissions, subscription_plans, addresses

**Key Characteristics:**
- ✅ All tenant-scoped tables have `organization_id`
- ✅ Audit fields: created_at, updated_at, created_by, updated_by
- ✅ Soft deletes: `status` field (ACTIVE/INACTIVE)
- ✅ Proper constraints and foreign keys
- ⚠️ No database-level row-level security (RLS)
- ⚠️ No explicit indexes on frequently queried columns

---

### A.6. API CONTRACTS

#### IdentityService:
```
POST /api/auth/login               {email, password}
POST /api/auth/otp/verify          {email, otp}
POST /api/auth/otp/resend          {email}
GET  /api/users/{userId}
POST /api/users                    {email, phone, password}
PUT  /api/users/{userId}
POST /api/user-roles               {userId, roleId}
```

#### StudentService:
```
POST   /api/students               {StudentRequestDto}
GET    /api/students               (no pagination!)
GET    /api/students/{studentId}
PUT    /api/students/{studentId}   {StudentRequestDto}
DELETE /api/students/{studentId}
(Similar patterns for parents, enrollments)
```

#### PlatformService:
```
GET  /api/modules
POST /api/modules/create
GET  /api/roles
POST /api/roles/create
GET  /api/permissions
GET  /api/subscription-plans
(All documented in POSTMAN_API_TESTING.md)
```

---

## PART B: MIGRATION PLAN

### B.1. TARGET STRUCTURE (Modular Monolith)

```
wissenup-backend/
├── src/main/java/com/wissenup/
│   ├── api/
│   │   └── v1/                          (API versioning)
│   │       ├── identity/
│   │       │   ├── AuthController
│   │       │   └── UserController
│   │       ├── student/
│   │       │   ├── StudentController
│   │       │   └── ParentController
│   │       ├── staff/
│   │       ├── attendance/
│   │       ├── exam/
│   │       ├── fee/
│   │       ├── timetable/
│   │       ├── academic/
│   │       ├── organization/
│   │       └── platform/
│   │           ├── ModuleController
│   │           ├── RoleController
│   │           └── PermissionController
│   │
│   ├── domain/                          (Business logic, strictly isolated)
│   │   ├── identity/
│   │   │   ├── AuthService
│   │   │   ├── UserService
│   │   │   ├── OtpService
│   │   │   ├── IdentityRepository
│   │   │   ├── entity/User, Role, UserRole
│   │   │   ├── dto/LoginRequest, LoginResponse, OtpRequest
│   │   │   ├── mapper/UserMapper
│   │   │   └── exception/InvalidCredentialsException, OtpExpiredException
│   │   │
│   │   ├── student/
│   │   │   ├── StudentService
│   │   │   ├── ParentService
│   │   │   ├── StudentEnrollmentService
│   │   │   ├── StudentParentService
│   │   │   ├── StudentRepository (+ ParentRepository, etc.)
│   │   │   ├── entity/StudentEntity, ParentEntity, StudentParentEntity, etc.
│   │   │   ├── dto/request/, dto/response/
│   │   │   ├── mapper/StudentMapper, ParentMapper
│   │   │   └── exception/StudentNotFoundException, StudentAlreadyExistsException
│   │   │
│   │   ├── staff/
│   │   ├── attendance/
│   │   ├── exam/
│   │   ├── fee/
│   │   ├── timetable/
│   │   ├── academic/
│   │   ├── organization/
│   │   └── platform/
│   │
│   ├── shared/
│   │   ├── security/
│   │   │   ├── JwtService, JwtServiceImpl
│   │   │   ├── JwtAuthenticationFilter
│   │   │   ├── JwtClaims, JwtSecurityConfiguration
│   │   │   ├── SecurityContextUtil
│   │   │   ├── OrganizationTenantInterceptor
│   │   │   ├── TenantRepositoryAspect
│   │   │   ├── TenantRequestBodyAdvice
│   │   │   └── TenantContextHolder (NEW)
│   │   │
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler (shared)
│   │   │   ├── ApiResponse (standardized)
│   │   │   ├── ApiException (base)
│   │   │   └── ErrorCode (enum)
│   │   │
│   │   ├── pagination/
│   │   │   ├── PageRequest
│   │   │   ├── PageResponse
│   │   │   └── Pageable
│   │   │
│   │   ├── validation/
│   │   │   └── validators for cross-domain rules
│   │   │
│   │   ├── logging/
│   │   │   ├── CorrelationIdFilter
│   │   │   └── LoggingAspect
│   │   │
│   │   ├── util/
│   │   │   └── common utilities
│   │   │
│   │   └── config/
│   │       ├── JpaConfig
│   │       ├── WebMvcConfig
│   │       └── SecurityConfig
│   │
│   └── WissenUpApplication.java         (Single Spring Boot entry point)
│
├── src/main/resources/
│   ├── application.yml                  (shared defaults)
│   ├── application-dev.yml              (dev: localhost, logging=DEBUG)
│   ├── application-test.yml             (test: in-memory configs)
│   ├── application-prod.yml             (prod: externalized env vars)
│   └── db/migration/                    (Flyway: V1, V2, V3, ... V10)
│
├── src/test/java/
│   ├── integration/                     (TestContainers PostgreSQL)
│   │   ├── identity/AuthServiceIT
│   │   ├── student/StudentServiceIT
│   │   └── ...
│   ├── unit/
│   │   ├── identity/AuthServiceTest
│   │   ├── student/StudentMapperTest
│   │   └── ...
│   ├── security/
│   │   ├── TenantIsolationSecurityTest
│   │   ├── JwtSecurityTest
│   │   └── ...
│   └── api/
│       ├── StudentControllerTest
│       └── ...
│
├── src/test/resources/
│   └── test data, fixtures
│
├── pom.xml                              (single source of truth)
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── README.md
└── docs/
    ├── API.md (OpenAPI spec)
    ├── ARCHITECTURE.md
    ├── DEVELOPMENT.md
    └── DEPLOYMENT.md
```

---

### B.2. MIGRATION STEPS (DETAILED)

#### **Phase 1: Preparation (Week 1)**

**1.1. Create Consolidated Repository Structure**
```bash
# Create new directory structure
mkdir -p wissenup-backend/src/main/java/com/wissenup/
mkdir -p wissenup-backend/src/main/resources/db/migration/
mkdir -p wissenup-backend/src/test/java/com/wissenup/
```

**1.2. Create Base pom.xml**
- Consolidate dependencies from 11 services
- Set version to 0.0.1-SNAPSHOT
- Add Spring Boot 4.1.0 parent
- Add test dependencies (JUnit 5, Testcontainers, Mockito)
- Add OpenAPI/Springdoc dependency

**1.3. Create application*.yml Files**
```yaml
# application.yml (defaults)
spring:
  application:
    name: wissenup-backend
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
  flyway:
    enabled: true
    locations: classpath:db/migration

app:
  jwt:
    secret: ${JWT_SECRET}
    expiry-minutes: ${JWT_EXPIRY_MINUTES:60}
```

**1.4. Create .env.example**
```bash
DB_URL=jdbc:postgresql://localhost:5432/wissenup
DB_USERNAME=postgres
DB_PASSWORD=1234  # CHANGE IN PRODUCTION!
JWT_SECRET=your-256-bit-base64-encoded-secret
JWT_EXPIRY_MINUTES=60
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
CORS_ALLOWED_ORIGINS=http://localhost:5173
SUPER_ADMIN_EMAIL=admin@wissenup.com
```

**1.5. Create Dockerfile**
- Multi-stage build (Maven build → JVM runtime)
- Non-root user
- Health checks
- JVM memory-aware

**1.6. Create docker-compose.yml**
- PostgreSQL service
- Backend service
- Environment variables from .env

---

#### **Phase 2: Migrate Identity/Security (Week 2)**

**2.1. Create Shared Security Module**
```
src/main/java/com/wissenup/shared/security/
├── JwtService, JwtServiceImpl              (from common-security)
├── JwtClaims                              (from common-security)
├── JwtAuthenticationFilter                (from common-security)
├── JwtSecurityConfiguration               (from common-security)
├── SecurityContextUtil                    (from common-security)
├── OrganizationTenantInterceptor          (from common-security)
├── TenantRepositoryAspect                 (from common-security)
├── TenantRequestBodyAdvice                (from common-security)
├── TenantContextHolder                    (NEW - explicit context management)
└── AuthenticationUtil                     (NEW - helper methods)
```

**2.2. Create Identity Domain**
```
src/main/java/com/wissenup/domain/identity/
├── AuthService, impl/AuthServiceImpl       (from IdentityService)
├── UserService, impl/UserServiceImpl       (from IdentityService)
├── OtpService, impl/OtpServiceImpl         (from IdentityService)
├── entity/User, UserRole, Role            (from identity schema)
├── repository/UserRepository, UserRoleRepository, RoleRepository
├── dto/
│   ├── request/LoginRequest, OtpRequest, RegisterRequest
│   └── response/LoginResponse, UserResponse
├── mapper/UserMapper, RoleMapper
└── exception/InvalidCredentialsException, OtpExpiredException, etc.
```

**2.3. Create API Controllers (v1)**
```
src/main/java/com/wissenup/api/v1/identity/
├── AuthController                        (from IdentityService)
└── UserController                        (from IdentityService)
```

**2.4. Update application.yml**
- Mail configuration (MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD from env)
- OTP_EXPIRY_MINUTES from env

**2.5. Create Tests**
- AuthServiceTest (unit)
- UserMapperTest (unit)
- AuthServiceIT (integration with TestContainers)
- JwtSecurityTest (security)

---

#### **Phase 3: Migrate Student Domain (Week 2-3)**

**3.1. Create Student Domain**
```
src/main/java/com/wissenup/domain/student/
├── StudentService, impl/StudentServiceImpl
├── ParentService, impl/ParentServiceImpl
├── StudentEnrollmentService, impl/StudentEnrollmentServiceImpl
├── StudentParentService, impl/StudentParentServiceImpl
├── StudentRegistrationService, impl/StudentRegistrationServiceImpl
├── entity/StudentEntity, ParentEntity, StudentParentEntity, StudentEnrollmentEntity
├── repository/StudentRepository, ParentRepository, etc.
├── dto/request/ + response/
├── mapper/StudentMapper, ParentMapper, etc.
└── exception/StudentNotFoundException, DuplicateStudentException, etc.
```

**3.2. Create API Controllers (v1)**
```
src/main/java/com/wissenup/api/v1/student/
├── StudentController
├── ParentController
├── StudentEnrollmentController
├── StudentParentController
└── StudentRegistrationController
```

**3.3. Add Pagination Support**
```
shared/pagination/
├── PageRequest.java
├── PageResponse.java
└── PageableUtils.java

// Update StudentController
@GetMapping
public ResponseEntity<PageResponse<StudentResponseDto>> getAllStudents(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "25") int size
) {
    return ResponseEntity.ok(studentService.getAllStudents(new PageRequest(page, size)));
}
```

**3.4. Create Tests**
- StudentServiceTest, StudentMapperTest (unit)
- StudentRepositoryTest (repository)
- StudentServiceIT, StudentControllerTest (integration)
- StudentTenantIsolationSecurityTest (security)

---

#### **Phase 4: Migrate Remaining Domains (Week 3-4)**

Repeat the pattern for:
- **Staff Domain** (StaffService → domain/staff/)
- **Attendance Domain** (AttendanceService → domain/attendance/)
- **Exam Domain** (ExamService → domain/exam/)
- **Fee Domain** (FeeService → domain/fee/)
- **Timetable Domain** (TimetableService → domain/timetable/)
- **Academic Domain** (Academic → domain/academic/)
- **Organization Domain** (Organization → domain/organization/)
- **Platform Domain** (PlatformService → domain/platform/)

For each:
1. Copy entities to domain/*/entity/
2. Copy repositories to domain/*/repository/
3. Copy services to domain/*/service/
4. Copy DTOs to domain/*/dto/
5. Copy mappers to domain/*/mapper/
6. Copy exceptions to domain/*/exception/
7. Create API controllers in api/v1/*/
8. Create tests in src/test/

---

#### **Phase 5: Shared Exception Handling (Week 4)**

**5.1. Create Unified Exception Hierarchy**
```
shared/exception/
├── ApiException                   (base)
├── AuthenticationException
├── AuthorizationException
├── ValidationException
├── ResourceNotFoundException
├── ConflictException
├── DuplicateResourceException
└── ExternalServiceException

shared/exception/GlobalExceptionHandler.java
├── @ControllerAdvice
├── @ExceptionHandler(ApiException.class)
├── @ExceptionHandler(MethodArgumentNotValidException.class)
├── @ExceptionHandler(AccessDeniedException.class)
└── standardized ApiResponse format
```

**5.2. Create ApiResponse**
```java
{
  "success": boolean,
  "code": "ERROR_CODE",
  "message": "Human-readable message",
  "data": null,
  "timestamp": "2026-08-18T10:00:00",
  "path": "/api/v1/students",
  "traceId": "correlation-id"
}
```

---

#### **Phase 6: API Documentation (Week 4-5)**

**6.1. Add Springdoc OpenAPI**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**6.2. Annotate Controllers**
```java
@RestController
@RequestMapping("/api/v1/students")
@Tag(name = "Students", description = "Student management APIs")
public class StudentController {
    
    @GetMapping
    @Operation(summary = "List all students", description = "Paginated list of students")
    @Parameters({
        @Parameter(name = "page", description = "Page number (0-indexed)"),
        @Parameter(name = "size", description = "Page size")
    })
    public ResponseEntity<PageResponse<StudentResponseDto>> getAllStudents(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "25") int size
    ) { }
}
```

**6.3. Generate OpenAPI** (available at /v3/api-docs)

---

#### **Phase 7: Testing & CI (Week 5-6)**

**7.1. Create Test Suite**
- Unit tests: 200+ tests
- Integration tests: 100+ tests (with TestContainers)
- Security tests: 30+ tenant isolation tests
- API tests: 50+ controller tests

**7.2. Create GitHub Actions**
```yaml
# .github/workflows/test.yml
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: password
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: mvn clean test
```

---

### B.3. BACKWARD COMPATIBILITY STRATEGY

#### **Approach: Dual-Run Phase**

**Week 5-6:** Run current 11 services + new consolidated backend side-by-side
1. Deploy new consolidated backend to port 8080
2. Frontend continues calling existing services on 1111-1120
3. Run automated tests comparing responses
4. Verify data consistency

**Week 7:** Cutover
1. Update frontend to call /api/v1/* on new backend (port 8080)
2. Verify all functionality working
3. Decommission 11 old services
4. Monitor for issues

#### **API Compatibility:**

Old:
```
POST /api/auth/login → IdentityService:1113
GET  /api/students → StudentService:1115
```

New:
```
POST /api/v1/auth/login → Backend:8080
GET  /api/v1/students → Backend:8080
```

**Minimum changes needed in frontend:**
- Replace hardcoded service URLs with single base URL
- Update `authService.js` to call /api/v1/auth/*
- Update all service calls to use /api/v1/*
- Verify pagination handling (new code returns PageResponse wrapper)

---

### B.4. FILES THAT WILL CHANGE

#### **Files to Delete** (after cutover):
- 11 service directories (IdentityService/, StudentService/, etc.)
- 11 pom.xml files
- 11 application.properties

#### **Files to Create:**
- 1 consolidated pom.xml
- 1 WissenUpApplication.java
- 1 shared security module (shared/security/)
- 9 domain modules (domain/{student,staff,attendance,...}/)
- 1 global exception handler (shared/exception/)
- 9 API controller packages (api/v1/{student,staff,...}/)
- application-dev.yml, application-test.yml, application-prod.yml
- .env.example
- Dockerfile, docker-compose.yml
- ~200+ test files
- README.md, ARCHITECTURE.md, DEPLOYMENT.md

#### **Files to Preserve/Refactor:**
- db.sql/flyway/ migrations (same, just v7+ for new schema)
- common-security code (move to shared/security/)

---

### B.5. RISK ASSESSMENT

| Risk | Severity | Mitigation |
|------|----------|-----------|
| Data inconsistency during migration | HIGH | Dual-run phase, automated comparison tests |
| Breaking frontend | MEDIUM | Maintain API contract, version endpoints, test thoroughly |
| Service inter-dependencies | MEDIUM | Consolidate in order (identity → others), map dependencies |
| Database migration issues | MEDIUM | Test migrations in dev first, backup before each phase |
| Configuration management | HIGH | Use .env.example, Secrets Manager, validate on startup |
| Test coverage gaps | MEDIUM | Prioritize critical paths (auth, tenant isolation, CRUD) |
| Deployment downtime | LOW | Use blue-green deployment (current 11 services → new monolith) |

---

## PART C: IMPLEMENTATION PRIORITIES

### Phase 1 (CRITICAL - Must Have):
1. Create consolidated monolith structure
2. Migrate security/identity domain (core of everything)
3. Externalize all secrets → environment variables
4. Create application-dev.yml, application-prod.yml
5. Create Dockerfile + docker-compose
6. Implement shared exception handler
7. Add pagination support

### Phase 2 (HIGH - Required):
1. Migrate remaining domains (student, staff, academic, etc.)
2. Implement unified API versioning (/api/v1/*)
3. Create test suite (unit, integration, security)
4. Add OpenAPI/Swagger documentation
5. Set up GitHub Actions CI/CD

### Phase 3 (MEDIUM - Important):
1. Frontend migration to new backend
2. Optimize database (indexes, connection pooling)
3. Add monitoring/logging
4. Performance testing

### Phase 4 (LOW - Nice to Have):
1. Async job processing (if needed)
2. Caching layer (Redis)
3. Read replicas (for scale)
4. Analytics database (separate)

---

## PART D: EXPECTED OUTCOMES

### After Consolidation:
✅ Single deployable Spring Boot JAR  
✅ Clear domain boundaries (no cross-domain repository access)  
✅ All secrets externalized (no hardcoded credentials in code)  
✅ Versioned APIs (/api/v1/*)  
✅ Consistent error handling  
✅ Pagination on all list endpoints  
✅ Comprehensive test suite  
✅ OpenAPI/Swagger documentation  
✅ Docker-ready for deployment  
✅ Foundation for future microservices extraction  

### Performance Characteristics:
- Single JVM process (vs 11 JVMs = less memory overhead)
- Shared PostgreSQL connection pool (more efficient)
- Same database queries (no network latency between services)
- Better for 0-100 schools (monolith is simpler to operate)
- When to split: If team grows to 5+ engineers working independently

---

## NEXT STEPS

1. **Review & Approve** this migration plan
2. **Set Up** consolidated repository structure
3. **Begin Phase 1:** Security & configuration (Week 1)
4. **Begin Phase 2:** Domain migration (Week 2-4)
5. **Testing & CI/CD** (Week 4-5)
6. **Cutover & Go Live** (Week 6)

---

**Total Effort:** 6 weeks, 1 engineer

**Go/No-Go Criteria:** All tests pass, tenant isolation verified, frontend working, no data loss

