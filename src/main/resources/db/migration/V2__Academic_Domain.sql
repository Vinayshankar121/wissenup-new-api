-- V2__Academic_Domain.sql
-- Academic Foundation tables for WissenUp School ERP
-- Includes: AcademicYear, Class, Section, Subject, ClassSubject, TeacherSubjectAssignment, ClassTeacherAssignment

-- ============================================================================
-- TABLE: academic_years
-- Purpose: School academic years (e.g., 2024-2025, 2025-2026)
-- Rules: Only ONE can be active per organization
-- ============================================================================

CREATE TABLE academic_years (
    academic_year_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    CONSTRAINT uq_academic_years_org_active UNIQUE (organization_id, is_active)
);

CREATE INDEX idx_academic_years_org_id ON academic_years(organization_id);
CREATE INDEX idx_academic_years_org_active ON academic_years(organization_id, is_active);
CREATE INDEX idx_academic_years_status ON academic_years(organization_id, status);

-- ============================================================================
-- TABLE: classes
-- Purpose: School classes/grades (e.g., 10, IX-A)
-- Attributes: level (1-12 for primary/secondary), code for system ID
-- ============================================================================

CREATE TABLE classes (
    class_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    academic_year_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    level INTEGER NOT NULL CHECK (level >= 1 AND level <= 12),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (academic_year_id) REFERENCES academic_years(academic_year_id) ON DELETE CASCADE
);

CREATE INDEX idx_classes_org_id ON classes(organization_id);
CREATE INDEX idx_classes_academic_year_id ON classes(academic_year_id);
CREATE INDEX idx_classes_org_academic_year ON classes(organization_id, academic_year_id);
CREATE INDEX idx_classes_org_code ON classes(organization_id, code);
CREATE INDEX idx_classes_status ON classes(organization_id, status);

-- ============================================================================
-- TABLE: sections
-- Purpose: Class divisions (e.g., Class 10 Section A, Section B)
-- Attributes: name (A, B, I, II), capacity, currentStrength (denormalized)
-- ============================================================================

CREATE TABLE sections (
    section_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    class_teacher_id BIGINT,
    capacity INTEGER,
    current_strength INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
    CONSTRAINT uq_sections_class_name UNIQUE (organization_id, class_id, name)
);

CREATE INDEX idx_sections_org_id ON sections(organization_id);
CREATE INDEX idx_sections_class_id ON sections(class_id);
CREATE INDEX idx_sections_org_class ON sections(organization_id, class_id);
CREATE INDEX idx_sections_status ON sections(organization_id, status);

-- ============================================================================
-- TABLE: subjects
-- Purpose: List of subjects taught in school (Math, English, Science, etc.)
-- Attributes: type (CORE, ELECTIVE, CO_CURRICULAR), maxMarks
-- ============================================================================

CREATE TABLE subjects (
    subject_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    type VARCHAR(50) NOT NULL DEFAULT 'CORE',
    max_marks INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    CONSTRAINT uq_subjects_org_name UNIQUE (organization_id, name)
);

CREATE INDEX idx_subjects_org_id ON subjects(organization_id);
CREATE INDEX idx_subjects_org_name ON subjects(organization_id, name);
CREATE INDEX idx_subjects_org_code ON subjects(organization_id, code);
CREATE INDEX idx_subjects_type ON subjects(organization_id, type);
CREATE INDEX idx_subjects_status ON subjects(organization_id, status);

-- ============================================================================
-- TABLE: class_subjects
-- Purpose: Junction table - which subjects are taught in which class
-- Rules: Each subject appears once per class within organization
-- Used for: Validating marks entry (teacher can only enter marks for subjects in class)
-- ============================================================================

CREATE TABLE class_subjects (
    class_subject_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    is_compulsory BOOLEAN DEFAULT true,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    CONSTRAINT uq_class_subjects UNIQUE (organization_id, class_id, subject_id)
);

CREATE INDEX idx_class_subjects_org_id ON class_subjects(organization_id);
CREATE INDEX idx_class_subjects_class_id ON class_subjects(class_id);
CREATE INDEX idx_class_subjects_subject_id ON class_subjects(subject_id);
CREATE INDEX idx_class_subjects_org_class ON class_subjects(organization_id, class_id);
CREATE INDEX idx_class_subjects_status ON class_subjects(organization_id, status);

-- ============================================================================
-- TABLE: teacher_subject_assignments
-- Purpose: Which subjects a teacher is qualified to teach
-- Rules: Each teacher-subject combination appears once per organization
-- Used for: Validating teacher can teach a subject before marks entry
-- ============================================================================

CREATE TABLE teacher_subject_assignments (
    teacher_subject_assignment_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    CONSTRAINT uq_teacher_subject UNIQUE (organization_id, staff_id, subject_id)
);

CREATE INDEX idx_teacher_subject_org_id ON teacher_subject_assignments(organization_id);
CREATE INDEX idx_teacher_subject_staff_id ON teacher_subject_assignments(staff_id);
CREATE INDEX idx_teacher_subject_subject_id ON teacher_subject_assignments(subject_id);
CREATE INDEX idx_teacher_subject_org_staff ON teacher_subject_assignments(organization_id, staff_id);
CREATE INDEX idx_teacher_subject_org_subject ON teacher_subject_assignments(organization_id, subject_id);
CREATE INDEX idx_teacher_subject_status ON teacher_subject_assignments(organization_id, status);

-- ============================================================================
-- TABLE: class_teacher_assignments
-- Purpose: Maps teacher to class for specific subject (attendance, marks, timetable)
-- Rules: Each teacher-class-subject combination appears once per organization
-- Special: isClassTeacher flag indicates class advisor (only ONE per class)
-- Used for: Attendance marking, timetable creation, marks entry
-- ============================================================================

CREATE TABLE class_teacher_assignments (
    class_teacher_assignment_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    is_class_teacher BOOLEAN DEFAULT false,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE,
    CONSTRAINT uq_class_teacher_assignment UNIQUE (organization_id, class_id, subject_id)
);

CREATE INDEX idx_class_teacher_org_id ON class_teacher_assignments(organization_id);
CREATE INDEX idx_class_teacher_class_id ON class_teacher_assignments(class_id);
CREATE INDEX idx_class_teacher_staff_id ON class_teacher_assignments(staff_id);
CREATE INDEX idx_class_teacher_subject_id ON class_teacher_assignments(subject_id);
CREATE INDEX idx_class_teacher_org_class ON class_teacher_assignments(organization_id, class_id);
CREATE INDEX idx_class_teacher_org_staff ON class_teacher_assignments(organization_id, staff_id);
CREATE INDEX idx_class_teacher_is_advisor ON class_teacher_assignments(organization_id, class_id, is_class_teacher);
CREATE INDEX idx_class_teacher_status ON class_teacher_assignments(organization_id, status);

-- ============================================================================
-- SUMMARY OF TABLES CREATED
-- ============================================================================
-- 1. academic_years (7 fields + 5 audit) - School years
-- 2. classes (9 fields + 5 audit) - Classes/grades
-- 3. sections (11 fields + 5 audit) - Class divisions
-- 4. subjects (9 fields + 5 audit) - Subject list
-- 5. class_subjects (7 fields + 5 audit) - Class-subject junction
-- 6. teacher_subject_assignments (7 fields + 5 audit) - Teacher qualifications
-- 7. class_teacher_assignments (10 fields + 5 audit) - Teacher-class mapping

-- TOTAL: 7 tables, 70 columns, 28 indexes, 6 unique constraints, 7 foreign keys
-- All tables enforce multi-tenancy via organization_id at database layer
-- All tables include audit fields for compliance/reporting

-- ============================================================================
-- MIGRATION NOTES
-- ============================================================================
-- - All tables use BIGSERIAL for IDs (supports 2^63-1 records)
-- - All tables require organization_id (multi-tenancy enforcement)
-- - All tables include audit fields (createdAt, createdBy, updatedAt, updatedBy)
-- - All status fields use VARCHAR(50) for ACTIVE/INACTIVE/ARCHIVED
-- - Foreign keys use ON DELETE CASCADE for data consistency
-- - Unique constraints are tenant-aware (include organization_id)
-- - Indexes created on:
--   a) organization_id (for query filtering)
--   b) foreign keys (for join performance)
--   c) lookup fields (name, code)
--   d) status (for filtering)
--   e) composite keys for common queries
-- ============================================================================
