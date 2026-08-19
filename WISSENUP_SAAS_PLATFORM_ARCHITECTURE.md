# WissenUp SaaS Platform Architecture

**Document**: SaaS Platform Layer Design  
**Date**: August 18, 2026  
**Status**: Design & Implementation Phase

---

## PLATFORM OVERVIEW

WissenUp transforms from single-school ERP to multi-tenant SaaS platform.

```
WissenUp SaaS Platform
    ├── Super Admin Portal (Platform Management)
    │   ├── Dashboard (analytics, health, usage)
    │   ├── Schools (CRUD, activate/deactivate)
    │   ├── Onboarding Workflow (transactional)
    │   ├── Subscription Plans (configuration-driven)
    │   ├── Modules (enable/disable per school)
    │   ├── School Settings (branding, timezone, etc.)
    │   ├── Audit Logs (all admin actions)
    │   ├── Usage Statistics (students, staff, storage)
    │   ├── Support Tools (impersonation, health checks)
    │   └── Feature Flags (roll out features gradually)
    │
    ├── Multi-Tenant Database
    │   ├── Platform Layer Tables (platform-agnostic)
    │   │   ├── schools (organization profiles)
    │   │   ├── subscription_plans (FREE, BASIC, STANDARD, PREMIUM)
    │   │   ├── school_subscriptions (active plan per school)
    │   │   ├── modules (Attendance, Fees, Exams, etc.)
    │   │   ├── school_modules (enabled/disabled per school)
    │   │   ├── school_settings (logo, timezone, currency, etc.)
    │   │   ├── usage_metrics (students, staff, API calls)
    │   │   ├── platform_audit_logs (all super admin actions)
    │   │   ├── platform_users (super admins only)
    │   │   └── impersonation_sessions (audit trail)
    │   │
    │   └── School-Level Data (existing)
    │       ├── organizations (points to schools)
    │       ├── users (school users)
    │       ├── academic_years (school's years)
    │       ├── students
    │       ├── staff
    │       └── ... (all existing ERP tables)
    │
    ├── APIs
    │   ├── /api/v1/platform/*       (super admin only)
    │   │   ├── /api/v1/platform/schools
    │   │   ├── /api/v1/platform/onboarding
    │   │   ├── /api/v1/platform/plans
    │   │   ├── /api/v1/platform/modules
    │   │   ├── /api/v1/platform/audit
    │   │   ├── /api/v1/platform/usage
    │   │   └── /api/v1/platform/support
    │   │
    │   └── /api/v1/*               (school-level, existing)
    │       ├── /api/v1/auth
    │       ├── /api/v1/academic-years
    │       ├── /api/v1/students
    │       └── ... (all existing endpoints)
    │
    └── Frontend
        ├── Super Admin Dashboard
        │   ├── Schools management UI
        │   ├── Onboarding wizard
        │   ├── Analytics/usage
        │   └── Audit log viewer
        │
        └── School Apps
            ├── School admin panel
            ├── Academic foundation
            ├── Student management
            └── ... (all existing features)
```

---

## SECURITY MODEL

### Authorization Tiers

```
SUPER_ADMIN (Platform Level)
    ├── Can create/delete schools
    ├── Can onboard schools (transactional)
    ├── Can manage plans and modules
    ├── Can impersonate school admins (audited)
    ├── Can view all audit logs
    └── Can view platform usage

SCHOOL_ADMIN (School Level)
    ├── Can manage school's data (students, staff, etc.)
    ├── Cannot access other schools
    ├── Cannot access platform admin (no /api/v1/platform/*)
    └── Follows school's enabled modules

SCHOOL_USER (Teacher/Staff)
    ├── Can access assigned data only
    ├── Cannot access school settings
    └── Follows role permissions

GUEST (Public)
    ├── Can access only public content
    └── Cannot access any protected data
```

### Endpoint Protection

```
/api/v1/platform/*
    ↓
@RequiresSuperAdmin
    ↓
Check JWT role = SUPER_ADMIN
    ↓
Allow access OR throw 403 Forbidden
```

---

## SUBSCRIPTION PLANS

### Plan Definitions (Configuration-Driven)

```java
Plan: FREE
├── Max Students: 100
├── Max Staff: 10
├── Max Users: 20
├── Storage: 1 GB
├── Modules: [Academic, Students] (limited)
└── Features: Community support, Basic reporting

Plan: TRIAL (Time-Limited, 30 days)
├── Max Students: 500
├── Max Staff: 50
├── Max Users: 100
├── Storage: 10 GB
├── Modules: All
└── Features: Full access, email support

Plan: BASIC
├── Max Students: 500
├── Max Staff: 50
├── Max Users: 100
├── Storage: 10 GB
├── Modules: [Academic, Students, Attendance, Fees]
└── Features: Email support, Billing portal

Plan: STANDARD
├── Max Students: 2000
├── Max Staff: 200
├── Max Users: 500
├── Storage: 50 GB
├── Modules: All
└── Features: Priority support, Advanced reporting, API access

Plan: PREMIUM
├── Max Students: Unlimited
├── Max Staff: Unlimited
├── Max Users: Unlimited
├── Storage: 500 GB
├── Modules: All
└── Features: Dedicated support, Custom integrations, SLA
```

### Plan Configuration (Database)

```sql
CREATE TABLE subscription_plans (
    plan_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,      -- FREE, TRIAL, BASIC, STANDARD, PREMIUM
    description TEXT,
    max_students INTEGER,
    max_staff INTEGER,
    max_users INTEGER,
    storage_gb INTEGER,
    price_per_month DECIMAL(10,2),
    trial_days INTEGER,                     -- NULL for non-trial plans
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE school_subscriptions (
    subscription_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP,                      -- NULL = active indefinitely
    trial_ends_at TIMESTAMP,                -- For TRIAL plan tracking
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(...),
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(...)
);
```

### Runtime Check

```java
// Before accessing feature
School school = schoolService.getSchool(schoolId);
SubscriptionPlan plan = school.getSubscription().getPlan();

// Check student limit
if (studentCount >= plan.getMaxStudents()) {
    throw new PlanLimitException("Student limit exceeded: " + plan.getMaxStudents());
}

// Check module access
if (!plan.getModules().contains("FEES")) {
    throw new ModuleNotAvailableException("Fees module not available in " + plan.getName());
}
```

---

## MODULES MANAGEMENT

### Supported Modules

```
Module: ACADEMIC
├── Features: Classes, Sections, Subjects, Teachers
├── Available in: FREE (limited), TRIAL, BASIC, STANDARD, PREMIUM
└── Required by: ATTENDANCE, EXAMS, TIMETABLE

Module: STUDENTS
├── Features: Student profiles, enrollment, documents
├── Available in: FREE, TRIAL, BASIC, STANDARD, PREMIUM
└── Required by: ATTENDANCE, FEES, EXAMS

Module: ATTENDANCE
├── Features: Daily attendance, reports
├── Available in: TRIAL, BASIC (limited), STANDARD, PREMIUM
└── Requires: ACADEMIC, STUDENTS

Module: FEES
├── Features: Fee management, payments, receipts
├── Available in: TRIAL, BASIC, STANDARD, PREMIUM
└── Requires: ACADEMIC, STUDENTS

Module: EXAMS
├── Features: Exam management, marks entry, results
├── Available in: TRIAL, STANDARD, PREMIUM
└── Requires: ACADEMIC, STUDENTS

Module: TIMETABLE
├── Features: Class and teacher timetables
├── Available in: TRIAL, STANDARD, PREMIUM
└── Requires: ACADEMIC

Module: TRANSPORT (Future)
├── Features: Bus management, routes
├── Available in: STANDARD, PREMIUM
└── Requires: STUDENTS

Module: LIBRARY (Future)
├── Features: Book management, circulation
├── Available in: STANDARD, PREMIUM
└── Requires: STUDENTS

Module: HR (Future)
├── Features: Staff management, payroll
├── Available in: PREMIUM
└── Requires: STUDENTS
```

### Database

```sql
CREATE TABLE modules (
    module_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,      -- ACADEMIC, STUDENTS, ATTENDANCE, etc.
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),                      -- For UI
    display_order INTEGER,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP
);

CREATE TABLE school_modules (
    school_module_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    is_enabled BOOLEAN DEFAULT true,
    enabled_at TIMESTAMP,
    disabled_at TIMESTAMP,
    UNIQUE (organization_id, module_id),
    FOREIGN KEY (organization_id) REFERENCES organizations(...),
    FOREIGN KEY (module_id) REFERENCES modules(...)
);

-- Constraints per plan
CREATE TABLE plan_modules (
    plan_module_id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    is_included BOOLEAN DEFAULT true,
    UNIQUE (plan_id, module_id),
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(...),
    FOREIGN KEY (module_id) REFERENCES modules(...)
);
```

---

## SCHOOL ONBOARDING (TRANSACTIONAL)

### Workflow

```
START: Create School
  ↓
STEP 1: School Information (name, email, address, phone)
  ├── Validate required fields
  ├── Check email uniqueness
  └── Create school record
  ↓
STEP 2: Admin User
  ├── Create user with SCHOOL_ADMIN role
  ├── Set password (send via email)
  └── Associate with school
  ↓
STEP 3: Academic Year
  ├── Create default academic year (current year)
  ├── Set as active
  └── Validate dates
  ↓
STEP 4: Classes & Sections (Setup)
  ├── Create default classes (1-12 or custom)
  ├── Create sections per class (A, B, C, etc.)
  └── Configure capacity
  ↓
STEP 5: Subscription Plan
  ├── Select plan (FREE, TRIAL, BASIC, STANDARD, PREMIUM)
  ├── Calculate trial end date (if applicable)
  └── Set active subscription
  ↓
STEP 6: Enable Modules
  ├── Based on selected plan
  ├── Allow manual enable/disable (within plan limits)
  └── Set module preferences
  ↓
STEP 7: School Settings
  ├── Logo upload (optional)
  ├── Timezone, currency, date format
  ├── Receipt settings
  └── Notification preferences
  ↓
STEP 8: Verify & Activate
  ├── Validate all data
  ├── Confirm no missing required fields
  └── Set school.is_active = true
  ↓
END: School Ready
```

### Transactional Guarantee

```java
@Transactional
public SchoolDto onboardSchool(OnboardingRequest request) {
    try {
        // Step 1: Create school
        School school = schoolRepository.save(...);
        
        // Step 2: Create admin user
        User admin = userRepository.save(...);
        
        // Step 3: Create academic year
        AcademicYear year = academicYearRepository.save(...);
        
        // Step 4: Create classes and sections
        createDefaultClassesAndSections(school, year);
        
        // Step 5: Create subscription
        SubscriptionPlan plan = subscriptionPlanRepository.findByCode(request.getPlanCode());
        schoolSubscriptionRepository.save(
            SchoolSubscription.builder()
                .organization(school)
                .plan(plan)
                .startedAt(NOW)
                .isActive(true)
                .build()
        );
        
        // Step 6: Enable modules
        enableModulesForPlan(school, plan);
        
        // Step 7: Save school settings
        schoolSettingsRepository.save(...);
        
        // Step 8: Activate school
        school.setIsActive(true);
        school = schoolRepository.save(school);
        
        return toDto(school);
        
    } catch (Exception e) {
        // ROLLBACK: All changes reverted automatically
        // School NOT created
        // Users NOT created
        // Subscriptions NOT created
        throw new OnboardingException("Onboarding failed at step X: " + e.getMessage());
    }
}
```

### Protection Against Partial Setup

```
❌ If Step 3 fails (academic year creation):
    → Transaction rolls back
    → School deleted
    → Admin user deleted
    → No partial school in database

✅ If Step 8 succeeds:
    → School fully configured
    → All data consistent
    → Ready for immediate use
```

---

## SUPER ADMIN DASHBOARD

### Key Metrics

```
Dashboard Cards:
├── Total Schools: 45
├── Active Schools: 43
├── Trial Schools: 8 (ending in 15 days)
├── Total Students: 125,430
├── Total Staff: 8,920
├── API Calls (24h): 1,234,567
├── Storage Used: 450 GB / 1000 GB
└── System Health: 99.8% uptime
```

### School Management Table

```
School Name          | Plan         | Students | Staff | Status      | Actions
School A             | STANDARD     | 2,100    | 150   | ACTIVE      | View | Edit | Suspend
School B             | FREE         | 450      | 45    | ACTIVE      | View | Edit | Suspend
School C             | TRIAL        | 1,200    | 90    | ACTIVE (7d) | View | Edit | Suspend
School D             | BASIC        | 800      | 60    | INACTIVE    | View | Edit | Delete
```

### Audit Log Viewer

```
Timestamp           | Super Admin | Action               | School        | Details
2026-08-18 14:23:45 | admin1      | School onboarded     | School E      | Plan: STANDARD
2026-08-18 13:15:20 | admin2      | Plan upgraded        | School A      | BASIC → STANDARD
2026-08-18 12:08:10 | admin1      | Impersonated admin   | School B      | Lasted 45 minutes
2026-08-18 11:55:30 | admin2      | Feature flag enabled | (Platform)    | ATTENDANCE_V2: 10% rollout
```

---

## SCHOOL SETTINGS

### Database

```sql
CREATE TABLE school_settings (
    settings_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE,
    logo_path VARCHAR(500),
    school_name VARCHAR(255),
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(100),
    website VARCHAR(255),
    timezone VARCHAR(50) DEFAULT 'UTC',
    currency VARCHAR(10) DEFAULT 'USD',
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    receipt_logo_path VARCHAR(500),
    receipt_footer TEXT,
    notification_email_enabled BOOLEAN DEFAULT true,
    notification_sms_enabled BOOLEAN DEFAULT false,
    notification_whatsapp_enabled BOOLEAN DEFAULT false,
    academic_session_start_month INTEGER DEFAULT 6,  -- June
    academic_session_end_month INTEGER DEFAULT 5,    -- May
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(...)
);
```

---

## USAGE TRACKING

### Simple PostgreSQL Counters

```sql
CREATE TABLE usage_metrics (
    metric_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    date DATE NOT NULL,
    student_count INTEGER,
    staff_count INTEGER,
    user_count INTEGER,
    storage_used_mb INTEGER,
    api_calls INTEGER,
    UNIQUE (organization_id, date),
    FOREIGN KEY (organization_id) REFERENCES organizations(...)
);
```

### Runtime Updates

```java
// Called daily (scheduled job)
@Scheduled(cron = "0 0 * * * *")  // Every hour
public void updateUsageMetrics() {
    for (School school : schoolRepository.findAllActive()) {
        int studentCount = studentRepository.countByOrganizationId(school.getOrganizationId());
        int staffCount = staffRepository.countByOrganizationId(school.getOrganizationId());
        int userCount = userRepository.countByOrganizationId(school.getOrganizationId());
        long storageMb = calculateStorageUsage(school);
        
        UsageMetric metric = UsageMetric.builder()
            .organization(school)
            .date(LocalDate.now())
            .studentCount(studentCount)
            .staffCount(staffCount)
            .userCount(userCount)
            .storageUsedMb((int)storageMb)
            .build();
        
        usageMetricRepository.save(metric);
        
        // Check limits
        SubscriptionPlan plan = school.getSubscription().getPlan();
        if (studentCount >= plan.getMaxStudents()) {
            // Send warning email
            notificationService.sendPlanLimitWarning(school, "students", studentCount);
        }
    }
}
```

---

## SUPPORT & IMPERSONATION

### Impersonation Rules

```
Super Admin can impersonate School Admin IF:

✅ Explicit reason provided
   "Debugging attendance issue reported by user"

✅ Audit log created before impersonation starts
   {
     "action": "IMPERSONATION_START",
     "super_admin": "admin1",
     "school_admin": "school_b_admin",
     "reason": "Debugging attendance issue",
     "ip_address": "192.168.1.1",
     "timestamp": "2026-08-18T14:30:00Z"
   }

✅ Session time-limited (1 hour default)
   "Session expires at 15:30:00Z"

✅ Visible banner in school UI
   "You are being impersonated by super admin 'admin1'
    Reason: Debugging attendance issue
    Session ends: 15:30:00 UTC"

✅ All actions logged as super admin
   {
     "action": "VIEW_STUDENT",
     "actor": "IMPERSONATING_SUPER_ADMIN[admin1]",
     "school": "School B",
     "timestamp": "2026-08-18T14:35:00Z"
   }

✅ Audit log created after impersonation ends
   {
     "action": "IMPERSONATION_END",
     "super_admin": "admin1",
     "school_admin": "school_b_admin",
     "duration_minutes": 5,
     "actions_performed": 15,
     "timestamp": "2026-08-18T14:35:00Z"
   }

❌ Cannot impersonate another super admin
❌ Cannot extend session time
❌ Cannot bypass 1-hour limit
```

---

## AUDIT LOGGING

### Events to Audit

```
School Management:
├── School created
├── School updated
├── School activated
├── School deactivated
├── School deleted

Subscription:
├── Plan changed
├── Trial extended
├── Payment processed
├── Plan cancelled

Modules:
├── Module enabled
├── Module disabled

Impersonation:
├── Impersonation started
├── Impersonation ended
├── Actions during impersonation

Feature Flags:
├── Flag created
├── Flag enabled
├── Flag disabled
├── Rollout percentage changed

Support:
├── Support ticket created
├── Support ticket resolved

Data:
├── Bulk data deleted
├── Data exported
├── Backup created
```

### Audit Log Entry

```sql
CREATE TABLE platform_audit_logs (
    log_id BIGSERIAL PRIMARY KEY,
    super_admin_id BIGINT,
    action VARCHAR(100) NOT NULL,           -- SCHOOL_CREATED, PLAN_CHANGED, etc.
    entity_type VARCHAR(50),                -- SCHOOL, PLAN, MODULE, etc.
    entity_id BIGINT,
    details JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (super_admin_id) REFERENCES users(user_id)
);

-- Example details
{
  "school_id": 45,
  "school_name": "School E",
  "old_plan": "BASIC",
  "new_plan": "STANDARD",
  "reason": "Customer requested upgrade",
  "billing_start": "2026-08-18",
  "api_client": "web"
}
```

---

## FILE STRUCTURE (NEW DOMAIN)

```
src/main/java/com/wissenup/domain/platform/
├── controller/
│   ├── PlatformDashboardController.java
│   ├── SchoolManagementController.java
│   ├── OnboardingController.java
│   ├── SubscriptionController.java
│   ├── ModuleManagementController.java
│   ├── SchoolSettingsController.java
│   ├── UsageController.java
│   ├── AuditLogController.java
│   └── SupportController.java
│
├── dto/
│   ├── SchoolDto, CreateSchoolRequest, UpdateSchoolRequest
│   ├── OnboardingRequest, OnboardingResponse
│   ├── SubscriptionPlanDto, SchoolSubscriptionDto
│   ├── ModuleDto, SchoolModuleDto
│   ├── SchoolSettingsDto
│   ├── UsageMetricDto
│   ├── PlatformAuditLogDto
│   └── ImpersonationSessionDto
│
├── entity/
│   ├── School.java
│   ├── SubscriptionPlan.java
│   ├── SchoolSubscription.java
│   ├── Module.java
│   ├── SchoolModule.java
│   ├── SchoolSettings.java
│   ├── UsageMetric.java
│   ├── PlatformAuditLog.java
│   ├── PlatformUser.java
│   └── ImpersonationSession.java
│
├── repository/
│   ├── SchoolRepository.java
│   ├── SubscriptionPlanRepository.java
│   ├── SchoolSubscriptionRepository.java
│   ├── ModuleRepository.java
│   ├── SchoolModuleRepository.java
│   ├── SchoolSettingsRepository.java
│   ├── UsageMetricRepository.java
│   └── PlatformAuditLogRepository.java
│
├── service/
│   ├── SchoolService.java
│   ├── OnboardingService.java (CRITICAL)
│   ├── SubscriptionService.java
│   ├── ModuleService.java
│   ├── SchoolSettingsService.java
│   ├── UsageService.java
│   ├── PlatformAuditService.java
│   └── SupportService.java
│
├── mapper/
│   ├── SchoolMapper.java
│   ├── SubscriptionMapper.java
│   ├── ModuleMapper.java
│   ├── SchoolSettingsMapper.java
│   ├── UsageMetricMapper.java
│   └── PlatformAuditLogMapper.java
│
├── security/
│   ├── PlatformSecurityAspect.java
│   └── RequiresSuperAdmin.java (annotation)
│
└── validation/
    ├── SchoolValidator.java
    └── OnboardingValidator.java
```

---

## DATABASE MIGRATION

**File**: `V3__Platform_Layer.sql`

```sql
-- Platform tables (WissenUp-specific)
CREATE TABLE schools (...)
CREATE TABLE subscription_plans (...)
CREATE TABLE school_subscriptions (...)
CREATE TABLE modules (...)
CREATE TABLE school_modules (...)
CREATE TABLE plan_modules (...)
CREATE TABLE school_settings (...)
CREATE TABLE usage_metrics (...)
CREATE TABLE platform_audit_logs (...)
CREATE TABLE platform_users (...)
CREATE TABLE impersonation_sessions (...)

-- Indexes
CREATE INDEX idx_schools_email ON schools(email);
CREATE INDEX idx_school_subscriptions_org_id ON school_subscriptions(organization_id);
-- ... (20+ indexes for performance)

-- Foreign keys
ALTER TABLE school_subscriptions ADD CONSTRAINT ...
-- ... (more FKs)
```

---

## DELIVERABLES

This phase will deliver:

1. ✅ Platform entities (10 new tables)
2. ✅ Repositories with multi-tenancy support
3. ✅ DTOs for all platform operations
4. ✅ Service layer (school, onboarding, subscription, modules)
5. ✅ REST controllers (super admin only)
6. ✅ Onboarding workflow (transactional)
7. ✅ Subscription plan management
8. ✅ Module enable/disable per school
9. ✅ School settings management
10. ✅ Usage tracking
11. ✅ Audit logging
12. ✅ Impersonation support (audited)
13. ✅ Authorization enforcement
14. ✅ Unit & security tests
15. ✅ Production-ready code

---

**Next Step**: Implementation begins. Estimated effort: 12-16 hours.

**Key Principle**: Keep it simple. Use PostgreSQL. Single monolith. No external services.

**Goal**: Low-cost SaaS platform that supports hundreds of schools from day one.
