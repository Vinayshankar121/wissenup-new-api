-- V19: Insert Platform Initialization Data
-- Inserts: Roles, Super Admin User, Modules, Subscription Plans, and Plan-Module Mappings

-- ============================================
-- 1. Insert SUPER_ADMIN Role
-- ============================================
INSERT INTO roles (
    code,
    name,
    description,
    is_system_role,
    created_at
)
VALUES (
    'SUPER_ADMIN',
    'Super Administrator',
    'Full platform administration access',
    TRUE,
    CURRENT_TIMESTAMP
)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_system_role = TRUE,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- 2. Insert School-Level Roles
-- ============================================
INSERT INTO roles (
    code,
    name,
    description,
    is_system_role,
    created_at
)
VALUES
    (
        'SCHOOL_ADMIN',
        'School Administrator',
        'Full administration access within a school',
        TRUE,
        CURRENT_TIMESTAMP
    ),
    (
        'TEACHER',
        'Teacher',
        'Teaching staff access',
        TRUE,
        CURRENT_TIMESTAMP
    ),
    (
        'CASHIER',
        'Cashier',
        'Fee and payment management access',
        TRUE,
        CURRENT_TIMESTAMP
    ),
    (
        'PARENT',
        'Parent',
        'Parent portal access',
        TRUE,
        CURRENT_TIMESTAMP
    ),
    (
        'STUDENT',
        'Student',
        'Student portal access',
        TRUE,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (code) DO UPDATE
SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_system_role = EXCLUDED.is_system_role,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- 3. Insert Super Admin User
-- ============================================
INSERT INTO users (
    organization_id,
    email,
    phone_number,
    password,
    status,
    created_at
)
VALUES (
    0,
    'vinaynukala65@gmail.com',
    '0000000000',
    '$2a$10$WyTbnV09JsEvRXmY/ysAdOIQSTwcDx7PepdNs2g1gjC/m4Xhw/MJK',
    'ACTIVE',
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE
SET organization_id = 0,
    password = EXCLUDED.password,
    status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- 4. Assign SUPER_ADMIN Role to Super Admin User
-- ============================================
INSERT INTO user_roles (
    user_id,
    role_id,
    status,
    created_at
)
SELECT
    u.user_id,
    r.role_id,
    'ACTIVE',
    CURRENT_TIMESTAMP
FROM users u
CROSS JOIN roles r
WHERE u.email = 'vinaynukala65@gmail.com'
  AND r.code = 'SUPER_ADMIN'
ON CONFLICT (user_id, role_id) DO UPDATE
SET status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- 5. Insert Platform Modules
-- ============================================
INSERT INTO modules (code, name, description, icon, display_order, is_active, created_at)
VALUES
    ('ACADEMIC',     'Academic',         'Academic years, classes, sections and subjects', 'layers',      10, TRUE, CURRENT_TIMESTAMP),
    ('STUDENTS',     'Students',         'Student records and enrollment',                  'users',       20, TRUE, CURRENT_TIMESTAMP),
    ('STAFF',        'Staff Management', 'Teachers and non-teaching staff',                 'briefcase',   30, TRUE, CURRENT_TIMESTAMP),
    ('ATTENDANCE',   'Attendance',       'Student and staff attendance',                    'user-check',  40, TRUE, CURRENT_TIMESTAMP),
    ('TIMETABLE',    'Timetable',        'Class and examination schedules',                 'clock',       50, TRUE, CURRENT_TIMESTAMP),
    ('CALENDAR',     'Calendar',         'School events and academic calendar',             'calendar',    60, TRUE, CURRENT_TIMESTAMP),
    ('NOTICES',      'Notices',          'Announcements and communication',                 'bell',        70, TRUE, CURRENT_TIMESTAMP),
    ('FEES',         'Fees',             'Fees, invoices and payments',                     'credit-card', 80, TRUE, CURRENT_TIMESTAMP),
    ('EXAMINATIONS', 'Examinations',     'Exams, marks, results and report cards',          'award',       90, TRUE, CURRENT_TIMESTAMP),
    ('PARENTS',      'Parents',          'Parent records and portal access',                'user-plus',  100, TRUE, CURRENT_TIMESTAMP),
    ('REPORTS',      'Reports',          'Operational and academic reports',                'bar-chart',  110, TRUE, CURRENT_TIMESTAMP),
    ('SETTINGS',     'Settings',         'School profile and configuration',                'sliders',    120, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    display_order = EXCLUDED.display_order,
    is_active = TRUE,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- 6. Insert PREMIUM Subscription Plan
-- ============================================
INSERT INTO subscription_plans (
    code, name, description, max_students, max_staff, max_users, storage_gb,
    price_per_month, yearly_price, duration_days, grace_period_days,
    trial_days, is_active, created_at
)
VALUES (
    'PREMIUM', 'Premium',
    'Complete school management suite with every available module',
    5000, 500, 5500, 500,
    4999.00, 49990.00, 365, 15,
    NULL, TRUE, CURRENT_TIMESTAMP
)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    max_students = EXCLUDED.max_students,
    max_staff = EXCLUDED.max_staff,
    max_users = EXCLUDED.max_users,
    storage_gb = EXCLUDED.storage_gb,
    price_per_month = EXCLUDED.price_per_month,
    yearly_price = EXCLUDED.yearly_price,
    duration_days = EXCLUDED.duration_days,
    grace_period_days = EXCLUDED.grace_period_days,
    is_active = TRUE,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================
-- 7. Link All Modules to PREMIUM Plan
-- ============================================
INSERT INTO plan_modules (plan_id, module_id)
SELECT plan.plan_id, module.module_id
FROM subscription_plans plan
CROSS JOIN modules module
WHERE plan.code = 'PREMIUM' AND module.is_active = TRUE
ON CONFLICT (plan_id, module_id) DO NOTHING;

-- ============================================
-- Migration Complete
-- ============================================
COMMIT;
