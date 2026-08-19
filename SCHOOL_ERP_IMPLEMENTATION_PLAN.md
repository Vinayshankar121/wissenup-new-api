# WissenUp School ERP - Implementation Plan

## Overview

Build production-grade School ERP business modules in the Spring Boot modular monolith. All modules must enforce multi-tenancy, security, pagination, validation, and testability.

---

## Domain Architecture

### Completed ✅
- **identity**: Users, roles, authentication, JWT

### To Build (In Priority Order)

#### Phase 1: Academic Foundation (Week 1-2)
- academic: AcademicYear, Class, Section, Subject, ClassSubject, TeacherSubjectAssignment, ClassTeacherAssignment

#### Phase 2: Student Management (Week 2-3)
- student: Student, StudentEnrollment, StudentDocument, StudentStatus
- parent: Parent, ParentChild relationship

#### Phase 3: Staff Management (Week 3)
- staff: Staff, Department, Designation, StaffSubject

#### Phase 4: Attendance (Week 3-4)
- attendance: Attendance, AttendanceStatus

#### Phase 5: Financial (Week 4-5)
- fees: FeeType, FeeStructure, StudentFee, Payment, Receipt, Discount, Concession

#### Phase 6: Exams (Week 5)
- exam: Exam, ExamTimetable, Marks, Result, Grade, ReportCard

#### Phase 7: Timetable (Week 6)
- timetable: Period, Timetable, TimeTableEntry

#### Phase 8: Cross-Cutting Concerns (Week 6-7)
- audit: AuditLog (tracks all important changes)
- reporting: Report APIs (scalable, paginated)

---

## Database Schema Design

### Multi-Tenancy Enforcement

Every table with tenant-scoped data includes:
```sql
organization_id BIGINT NOT NULL
```

Indexes:
```sql
CREATE INDEX idx_<table>_organization_id ON <table>(organization_id);
```

Constraints:
```sql
FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
```

### Unique Constraints (Tenant-Aware)

Example: Student admission number must be unique within a school:
```sql
UNIQUE(organization_id, admission_number)
```

Example: Staff employee ID must be unique within a school:
```sql
UNIQUE(organization_id, employee_id)
```

---

## Entity Relationships (ERD)

```
organizations
├── academic_years (1:N)
│   └── classes (1:N)
│       ├── sections (1:N)
│       │   ├── students (1:N)
│       │   │   ├── student_enrollments (1:N)
│       │   │   ├── student_documents (1:N)
│       │   │   ├── parents (M:N via parent_children)
│       │   │   └── fees (1:N)
│       │   │       ├── payments (1:N)
│       │   │       └── receipts (1:N)
│       │   └── class_teachers (1:N)
│       │       └── teachers (M:1)
│       └── class_subjects (1:N)
│           └── subjects (M:1)
│
├── subjects (1:N)
├── staff (1:N)
│   ├── departments (1:N)
│   ├── designations (1:N)
│   └── staff_subjects (M:N via subject_id)
│
├── attendance (1:N)
│   └── attendance_details (1:N)
│
├── exams (1:N)
│   ├── exam_timetable (1:N)
│   ├── marks (1:N)
│   ├── results (1:N)
│   └── grades (1:N)
│
├── timetables (1:N)
│   └── timetable_entries (1:N)
│
└── audit_logs (1:N)
```

---

## Security Model

### Organization Isolation (3-Layer)

1. **JWT Claims**: organizationId from JWT token
2. **Repository Filtering**: TenantRepositoryAspect auto-filters by organization_id
3. **AOP Request Body Advice**: TenantRequestBodyAdvice injects organization_id

### Permission Model

```
SUPER_ADMIN (organization_id = 0)
  ├─ Can access any organization
  ├─ Can manage organizations
  └─ Can create users per organization

ORG_ADMIN (organization_id > 0)
  ├─ Can manage only their organization
  ├─ Can create staff, manage students, etc.
  └─ Cannot see other organizations' data

STAFF/TEACHER (organization_id > 0)
  ├─ Can see only assigned classes/subjects
  ├─ Can enter marks for assigned subjects
  ├─ Can mark attendance for assigned classes
  └─ Cannot see other organizations' data

PARENT (organization_id > 0)
  ├─ Can see only linked children
  ├─ Cannot see other students
  └─ Cannot see other organizations' data
```

---

## API Design Principles

### RESTful Endpoints

```
POST   /api/v1/academic-years           # Create
GET    /api/v1/academic-years           # List (paginated)
GET    /api/v1/academic-years/{id}      # Get by ID
PUT    /api/v1/academic-years/{id}      # Update
DELETE /api/v1/academic-years/{id}      # Soft delete/archive
PATCH  /api/v1/academic-years/{id}/activate # Custom action
```

### Pagination

All list endpoints support:
```
GET /api/v1/students?page=0&size=25&sort=name,asc&filter=status:ACTIVE
```

Response:
```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageNumber": 0,
    "pageSize": 25,
    "totalElements": 150,
    "totalPages": 6,
    "isLast": false,
    "isFirst": true
  }
}
```

### Filtering & Search

Support complex queries:
```
GET /api/v1/students?search=name:john&class=10&section=A&status=ACTIVE
```

### Validation

All requests validated with Zod (frontend) and Bean Validation (backend):
```java
@NotNull(message = "Name is required")
@NotBlank(message = "Name cannot be blank")
@Size(min = 2, max = 100, message = "Name must be 2-100 characters")
private String name;

@NotNull(message = "Admission number required")
@Pattern(regexp = "^[A-Z0-9]+$", message = "Invalid admission number format")
private String admissionNumber;

@Min(value = 1, message = "Class ID must be valid")
private Long classId;
```

---

## Implementation Pattern

Each domain follows the same structure:

```
src/main/java/com/wissenup/domain/{domain}/
├── entity/
│   ├── {Entity}.java (JPA @Entity, audit fields, validation)
│   ├── {Entity}Status.java (enum)
│   └── {Embedded}.java (embedded value objects)
│
├── dto/
│   ├── {Entity}Dto.java (response DTO)
│   ├── Create{Entity}Request.java (request DTO)
│   ├── Update{Entity}Request.java (update DTO)
│   └── {Entity}SearchRequest.java (search filters)
│
├── repository/
│   └── {Entity}Repository.java (JpaRepository)
│       ├── Custom query methods
│       └── Tenant-aware queries
│
├── service/
│   ├── {Entity}Service.java (interface)
│   └── {Entity}ServiceImpl.java (implementation)
│       ├── CRUD operations
│       ├── Business logic
│       ├── Validation
│       └── Audit logging
│
├── mapper/
│   └── {Entity}Mapper.java (entity ↔ DTO)
│
├── controller/
│   └── {Entity}Controller.java (@RestController)
│       ├── POST /api/v1/{entities}
│       ├── GET /api/v1/{entities}
│       ├── GET /api/v1/{entities}/{id}
│       ├── PUT /api/v1/{entities}/{id}
│       ├── DELETE /api/v1/{entities}/{id}
│       └── Custom actions (@PatchMapping)
│
├── exception/
│   └── {Domain}Exception.java (ApiException subclass)
│
└── validator/
    └── {Entity}Validator.java (custom validations)
```

---

## Database Migration Strategy

### Flyway Versions

```
V1__Initial_schema.sql              (identity domain)
V2__Academic_domain.sql             (academic foundation)
V3__Student_domain.sql              (student management)
V4__Parent_domain.sql               (parent management)
V5__Staff_domain.sql                (staff management)
V6__Attendance_domain.sql           (attendance)
V7__Fees_domain.sql                 (financial)
V8__Exam_domain.sql                 (exams)
V9__Timetable_domain.sql            (timetable)
V10__Audit_domain.sql               (audit logging)
V11__Indexes_and_constraints.sql    (performance & integrity)
```

### Key Design Decisions

1. **No Soft Deletes in Flyway** - Archive via status field
2. **Immutable Financial Records** - No UPDATE on payments, use REFUND transactions
3. **Audit Trail** - Before/After values in audit_logs table
4. **Denormalization** - Cache frequently accessed data (e.g., student_admission_number in student table)
5. **Partitioning (Future)** - attendance_details, marks, payment_receipts by date range

---

## Testing Strategy

### Unit Tests (Service Layer)
- Business logic
- Validation
- Edge cases
- Mocking repositories

### Integration Tests (Repository + Service)
- Database transactions
- Constraint violations
- Data persistence
- Rollback behavior

### Security Tests
- Organization isolation (School A cannot see School B)
- Permission checks (TEACHER cannot create academic years)
- JWT validation
- Cross-tenant data access prevention

### Test Example

```java
@Test
void shouldEnforceOrganizationIsolation() {
  // School A creates a student
  Student studentA = studentService.createStudent(request, organizationId=1);
  
  // School B tries to fetch that student (via their JWT context)
  securityContext.setOrganizationId(2);
  
  // Should throw AccessDeniedException or return empty
  assertThrows(ResourceNotFoundException.class, 
    () -> studentService.getStudent(studentA.getId(), organizationId=2));
}
```

---

## Audit Logging

### Auditable Entities

```
Academic Year: create, activate, deactivate, delete
Student: create, modify, status change, delete/archive
Fee: create, modify
Payment: create, refund
Marks: create, modify, delete
Attendance: create, modify
Staff: create, modify, delete
```

### Audit Fields in All Entities

```java
@Column(name = "created_at", nullable = false)
private LocalDateTime createdAt;

@Column(name = "created_by", nullable = false)
private Long createdBy; // User ID from JWT

@Column(name = "updated_at")
private LocalDateTime updatedAt;

@Column(name = "updated_by")
private Long updatedBy;
```

### Audit Log Table

```sql
CREATE TABLE audit_logs (
  audit_log_id BIGSERIAL PRIMARY KEY,
  organization_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, ARCHIVE
  entity_name VARCHAR(100) NOT NULL,
  entity_id BIGINT NOT NULL,
  changes JSONB, -- {before: {...}, after: {...}}
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  ip_address VARCHAR(45),
  request_id VARCHAR(100),
  FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
);

CREATE INDEX idx_audit_logs_organization_id ON audit_logs(organization_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
```

---

## Performance Optimization

### Indexes

Every table has:
```sql
CREATE INDEX idx_<table>_organization_id ON <table>(organization_id);
```

Additional indexes:
```sql
-- Student lookup by admission number
CREATE UNIQUE INDEX idx_students_org_admission ON students(organization_id, admission_number);

-- Parent lookup
CREATE INDEX idx_students_parent_id ON students(parent_id);

-- Class/Section lookup
CREATE INDEX idx_students_class_section ON students(class_id, section_id);

-- Attendance lookup
CREATE INDEX idx_attendance_date_class ON attendance(attendance_date, class_id);

-- Marks lookup
CREATE INDEX idx_marks_exam_student ON marks(exam_id, student_id);
```

### Pagination

All list endpoints paginated (max 100 items per page):
```properties
spring.jpa.properties.hibernate.default_batch_size=20
spring.jpa.properties.hibernate.jdbc.fetch_size=50
```

### Caching (Optional, Phase 2)

```java
@Cacheable(value = "academic_years", key = "#organizationId")
List<AcademicYear> getActiveAcademicYears(Long organizationId);
```

---

## Implementation Phases (8 Weeks)

### Week 1-2: Academic Foundation
- [x] Domain design
- [ ] AcademicYear entity, service, controller, tests
- [ ] Class, Section entities
- [ ] Subject, ClassSubject entities
- [ ] TeacherSubjectAssignment, ClassTeacherAssignment
- [ ] Flyway migrations
- [ ] Integration tests

### Week 2-3: Student Management
- [ ] Student entity (core fields)
- [ ] StudentEnrollment (tracks class/section per academic year)
- [ ] StudentStatus enum (ACTIVE, TRANSFERRED, WITHDRAWN, ALUMNI)
- [ ] StudentDocument entity
- [ ] Student search/filter endpoints
- [ ] Duplicate detection (admission number)
- [ ] Bulk import (Excel)
- [ ] Export functionality
- [ ] Tests: organization isolation, validation, permissions

### Week 3: Parent Management
- [ ] Parent entity
- [ ] ParentChild relationship (M:N)
- [ ] Parent profile API
- [ ] Multiple guardians support (father, mother, guardian)
- [ ] Parent portal permission model

### Week 3: Staff Management
- [ ] Staff entity (profile, ID, department, designation)
- [ ] Department, Designation enums
- [ ] StaffSubject assignment (which subjects)
- [ ] Staff status (ACTIVE, INACTIVE, LEFT)
- [ ] Tenant-aware employee ID uniqueness

### Week 3-4: Attendance
- [ ] Attendance entity (date, class)
- [ ] AttendanceDetail (student, status: PRESENT/ABSENT/LATE/HALF_DAY)
- [ ] Bulk attendance (class → mark all students)
- [ ] Prevent duplicates (unique on organization_id, attendance_date, student_id)
- [ ] Mobile UX (one-tap mark, quick search)
- [ ] Attendance reports (by student, by class)

### Week 4-5: Fees (Financial)
- [ ] FeeType (Tuition, Activity, Transport, etc.)
- [ ] FeeStructure (class-wise breakdown)
- [ ] StudentFee (student's assigned fees for year)
- [ ] FeeInstallment (schedule)
- [ ] Discount, Concession entities
- [ ] Payment entity (immutable)
- [ ] Receipt generation
- [ ] Refund entity (reference original payment, not modify)
- [ ] Outstanding balance calculation
- [ ] Payment history (paginated)
- [ ] Immutability tests (payment cannot be modified once paid)

### Week 5: Exams
- [ ] Exam entity (type, date range)
- [ ] ExamTimetable (exam → subjects → schedule)
- [ ] Marks entity (exam, student, subject, marks)
- [ ] Permission check: teacher can only enter marks for assigned subjects/classes
- [ ] Result entity (aggregate marks per student)
- [ ] Grade entity (marks range → grade letter)
- [ ] ReportCard foundation (student summary)
- [ ] Validation: marks within range
- [ ] Tests: permission enforcement

### Week 6: Timetable
- [ ] Period entity (period number, start/end time)
- [ ] Timetable entity (class, academic year)
- [ ] TimeTableEntry (day, period, subject, teacher, room)
- [ ] Prevent conflicts: teacher cannot have 2 entries same period
- [ ] Prevent conflicts: class cannot have 2 subjects same period
- [ ] Substitution foundation (for future)
- [ ] Timetable generation API

### Week 6-7: Audit Logging
- [ ] AuditLog entity
- [ ] AuditEventPublisher (Spring event-driven)
- [ ] @Audited annotation for automatic tracking
- [ ] Capture: action, entity, user, timestamp, before/after values
- [ ] Query API: audit trail per entity
- [ ] Tests: audit records created correctly

### Week 7: Reporting (Scalable)
- [ ] Report API: student strength by class/section
- [ ] Report API: attendance by period
- [ ] Report API: fees collection (daily, monthly, summary)
- [ ] Report API: outstanding fees
- [ ] Report API: exam results by subject
- [ ] Report API: staff list
- [ ] Pagination: all reports paginated (no in-memory aggregation)
- [ ] Export jobs (for large datasets)
- [ ] Tests: pagination, accuracy

---

## Deliverables Checklist

### For Each Domain:

- [ ] Entity classes (with validation, audit fields)
- [ ] Repository (JpaRepository + custom queries)
- [ ] Service interface + implementation
- [ ] DTOs (request, response, search)
- [ ] Mapper (entity ↔ DTO)
- [ ] Controller (@RestController with all CRUD + custom actions)
- [ ] Exception classes
- [ ] Flyway migration (SQL)
- [ ] Unit tests (service layer)
- [ ] Integration tests (repository + service)
- [ ] Security tests (organization isolation)
- [ ] OpenAPI/Swagger documentation
- [ ] Request/response examples in README

### Quality Standards:

- ✅ All endpoints paginated
- ✅ All inputs validated (Bean Validation + Zod)
- ✅ All responses wrapped in ApiResponse
- ✅ All errors handled (GlobalExceptionHandler)
- ✅ Multi-tenant isolation enforced
- ✅ Permission checks implemented
- ✅ Audit logging where appropriate
- ✅ No hardcoded values
- ✅ Efficient database queries (indexes, pagination)
- ✅ >80% test coverage
- ✅ Production-grade code (no TODOs, no debug logs)

---

## Build & Deploy

### Build
```bash
mvn clean compile
mvn clean test
mvn clean package
```

### Database Migrations
```bash
# Flyway auto-runs on application startup
# Verify: SELECT * FROM flyway_schema_history;
```

### Run
```bash
java -jar target/wissenup-backend-1.0.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/wissenup \
  --spring.datasource.username=postgres \
  --spring.datasource.password=admin123 \
  --app.jwt.secret=<base64-encoded-secret> \
  --spring.mail.username=<email> \
  --spring.mail.password=<password>
```

### Verify
```bash
curl http://localhost:8080/api/v1/health/check
# {
#   "success": true,
#   "data": {
#     "status": "UP",
#     "application": "WissenUp Backend",
#     "version": "1.0.0"
#   }
# }
```

---

## Success Criteria

- ✅ All 8 domains implemented (academic, student, parent, staff, attendance, fees, exam, timetable)
- ✅ Audit logging for important actions
- ✅ All endpoints tested (unit + integration + security)
- ✅ Organization isolation enforced (School A ≠ School B)
- ✅ No production bugs
- ✅ Flyway migrations work
- ✅ API documentation complete (Swagger)
- ✅ Performance acceptable (<500ms for paginated queries)
- ✅ Financial records immutable
- ✅ All validations working

---

## Notes

- No microservices, Kafka, Redis, or Elasticsearch
- Keep modular monolith structure
- Use PostgreSQL efficiently (indexes, pagination)
- Test organization isolation thoroughly (critical for SaaS)
- Financial records must be audit-trail friendly
- Batch operations (bulk import, bulk attendance) important for school use
- Mobile considerations (attendance marking, fee payment)
