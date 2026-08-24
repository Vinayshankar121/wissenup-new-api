CREATE TABLE IF NOT EXISTS addresses (
    address_id BIGSERIAL PRIMARY KEY, organization_id BIGINT NOT NULL,
    address_line1 VARCHAR(255), address_line2 VARCHAR(255), locality VARCHAR(100), city VARCHAR(100),
    state VARCHAR(100), country VARCHAR(100), zip_code VARCHAR(20), status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS parents (
    parent_id BIGSERIAL PRIMARY KEY, organization_id BIGINT NOT NULL,
    first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100), phone_number VARCHAR(30) NOT NULL,
    email VARCHAR(255), occupation VARCHAR(150), address_id BIGINT, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, created_by BIGINT NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES addresses(address_id),
    CONSTRAINT uq_parents_org_phone UNIQUE (organization_id, phone_number)
);

CREATE TABLE IF NOT EXISTS students (
    student_id BIGSERIAL PRIMARY KEY, organization_id BIGINT NOT NULL,
    admission_no VARCHAR(50) NOT NULL, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100),
    gender VARCHAR(20) NOT NULL, date_of_birth DATE NOT NULL, blood_group VARCHAR(10), admission_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, created_by BIGINT NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    CONSTRAINT uq_students_org_admission UNIQUE (organization_id, admission_no)
);

CREATE TABLE IF NOT EXISTS student_parents (
    student_parent_id BIGSERIAL PRIMARY KEY, organization_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL, parent_id BIGINT NOT NULL, relationship VARCHAR(20) NOT NULL, is_primary BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES parents(parent_id),
    CONSTRAINT uq_student_parent UNIQUE (student_id, parent_id)
);

CREATE TABLE IF NOT EXISTS student_enrollments (
    enrollment_id BIGSERIAL PRIMARY KEY, organization_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL, academic_year_id BIGINT NOT NULL, class_id BIGINT NOT NULL, section_id BIGINT NOT NULL,
    roll_no VARCHAR(30), enrollment_date DATE NOT NULL, promotion_status VARCHAR(30), status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (academic_year_id) REFERENCES academic_years(academic_year_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id), FOREIGN KEY (section_id) REFERENCES sections(section_id),
    CONSTRAINT uq_student_enrollment_year UNIQUE (student_id, academic_year_id)
);

CREATE INDEX IF NOT EXISTS idx_students_org ON students(organization_id);
CREATE INDEX IF NOT EXISTS idx_enrollments_class_section ON student_enrollments(organization_id, class_id, section_id);
