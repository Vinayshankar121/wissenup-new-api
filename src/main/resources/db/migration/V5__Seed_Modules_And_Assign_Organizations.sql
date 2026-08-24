-- Seed the module catalog used by the organization workspace.
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
    ('EXAMINATIONS', 'Examinations',     'Exams, marks, results and report cards',           'award',       90, TRUE, CURRENT_TIMESTAMP),
    ('PARENTS',      'Parents',          'Parent records and portal access',                 'user-plus',  100, TRUE, CURRENT_TIMESTAMP),
    ('REPORTS',      'Reports',          'Operational and academic reports',                'bar-chart',  110, TRUE, CURRENT_TIMESTAMP),
    ('SETTINGS',     'Settings',         'School profile and configuration',                'sliders',    120, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    icon = EXCLUDED.icon,
    display_order = EXCLUDED.display_order,
    is_active = TRUE;

-- Existing organizations were created while the module catalog was empty.
-- Give them the same complete module assignment as newly onboarded schools.
INSERT INTO school_modules (organization_id, module_id, is_enabled, enabled_at, created_at)
SELECT organization.organization_id, module.module_id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM organizations organization
CROSS JOIN modules module
WHERE module.is_active = TRUE
ON CONFLICT (organization_id, module_id) DO UPDATE
SET is_enabled = TRUE,
    enabled_at = COALESCE(school_modules.enabled_at, CURRENT_TIMESTAMP),
    disabled_at = NULL;
