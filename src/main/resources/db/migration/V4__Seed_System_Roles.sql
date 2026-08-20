INSERT INTO roles (code, name, description, is_system_role, created_at)
VALUES
    ('SUPER_ADMIN', 'Super Administrator', 'Full platform administration access', TRUE, CURRENT_TIMESTAMP),
    ('SCHOOL_ADMIN', 'School Administrator', 'Full administration access within a school', TRUE, CURRENT_TIMESTAMP),
    ('TEACHER', 'Teacher', 'Teaching staff access', TRUE, CURRENT_TIMESTAMP),
    ('CASHIER', 'Cashier', 'Fee and payment management access', TRUE, CURRENT_TIMESTAMP),
    ('PARENT', 'Parent', 'Parent portal access', TRUE, CURRENT_TIMESTAMP),
    ('STUDENT', 'Student', 'Student portal access', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_system_role = EXCLUDED.is_system_role,
    updated_at = CURRENT_TIMESTAMP;
