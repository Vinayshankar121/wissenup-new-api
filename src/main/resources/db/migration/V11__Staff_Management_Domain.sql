CREATE TABLE IF NOT EXISTS departments (
    department_id BIGSERIAL PRIMARY KEY, organization_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL, department_type VARCHAR(30), status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, created_by BIGINT,
    CONSTRAINT uq_departments_org_name UNIQUE (organization_id, name),
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS designations (
    designation_id BIGSERIAL PRIMARY KEY, organization_id BIGINT NOT NULL, department_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL, description VARCHAR(500), status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, created_by BIGINT,
    CONSTRAINT uq_designations_org_name UNIQUE (organization_id, name),
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS staff (
    staff_id BIGSERIAL PRIMARY KEY, organization_id BIGINT NOT NULL, user_id BIGINT NOT NULL UNIQUE,
    department_id BIGINT NOT NULL, designation_id BIGINT NOT NULL, address_id BIGINT,
    employee_code VARCHAR(50) NOT NULL, first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100),
    gender VARCHAR(20), date_of_birth DATE, joining_date DATE NOT NULL, qualification VARCHAR(255),
    experience NUMERIC(5,1), email VARCHAR(150) NOT NULL, phone_number VARCHAR(30), role_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, created_by BIGINT,
    CONSTRAINT uq_staff_org_employee UNIQUE (organization_id, employee_code),
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE RESTRICT,
    FOREIGN KEY (department_id) REFERENCES departments(department_id) ON DELETE RESTRICT,
    FOREIGN KEY (designation_id) REFERENCES designations(designation_id) ON DELETE RESTRICT,
    FOREIGN KEY (address_id) REFERENCES addresses(address_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_staff_org ON staff(organization_id);
