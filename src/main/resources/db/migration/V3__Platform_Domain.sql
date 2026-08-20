-- Platform and identity tables required by the JPA entities.
-- Apply after V1__Initial_schema.sql and V2__Academic_Domain.sql.

-- Subject.description exists in the JPA model but was omitted from V2.
ALTER TABLE subjects
    ADD COLUMN IF NOT EXISTS description VARCHAR(500);

CREATE TABLE IF NOT EXISTS roles (
    role_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_code ON roles(code);

-- V1 creates user_roles before roles exists, so add its missing FK here.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_roles_role'
    ) THEN
        ALTER TABLE user_roles
            ADD CONSTRAINT fk_user_roles_role
            FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS modules (
    module_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(255),
    display_order INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subscription_plans (
    plan_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    max_students INTEGER NOT NULL,
    max_staff INTEGER NOT NULL,
    max_users INTEGER NOT NULL,
    storage_gb INTEGER NOT NULL,
    price_per_month NUMERIC(19,2),
    trial_days INTEGER,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS schools (
    school_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip_code VARCHAR(255),
    country VARCHAR(255),
    website VARCHAR(255),
    logo_path VARCHAR(255),
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_trial BOOLEAN NOT NULL DEFAULT FALSE,
    trial_ends_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_schools_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_schools_status ON schools(status);
CREATE INDEX IF NOT EXISTS idx_schools_is_active ON schools(is_active);

CREATE TABLE IF NOT EXISTS school_modules (
    school_module_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    enabled_at TIMESTAMP,
    disabled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_school_modules UNIQUE (organization_id, module_id),
    CONSTRAINT fk_school_modules_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_school_modules_module
        FOREIGN KEY (module_id) REFERENCES modules(module_id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_school_modules_organization
    ON school_modules(organization_id);

CREATE TABLE IF NOT EXISTS school_settings (
    settings_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE,
    logo_path VARCHAR(255),
    school_name VARCHAR(255),
    address VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    website VARCHAR(255),
    timezone VARCHAR(255) DEFAULT 'UTC',
    currency VARCHAR(255) DEFAULT 'USD',
    date_format VARCHAR(255) DEFAULT 'DD/MM/YYYY',
    receipt_logo_path VARCHAR(255),
    receipt_footer VARCHAR(255),
    notification_email_enabled BOOLEAN DEFAULT TRUE,
    notification_sms_enabled BOOLEAN DEFAULT FALSE,
    notification_whatsapp_enabled BOOLEAN DEFAULT FALSE,
    academic_session_start_month INTEGER DEFAULT 6,
    academic_session_end_month INTEGER DEFAULT 5,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_school_settings_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS school_subscriptions (
    subscription_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP,
    trial_ends_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_school_subscriptions_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_school_subscriptions_plan
        FOREIGN KEY (plan_id) REFERENCES subscription_plans(plan_id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_school_subscriptions_active
    ON school_subscriptions(organization_id, is_active);

CREATE TABLE IF NOT EXISTS usage_metrics (
    metric_id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    date DATE NOT NULL,
    student_count INTEGER NOT NULL DEFAULT 0,
    staff_count INTEGER NOT NULL DEFAULT 0,
    user_count INTEGER NOT NULL DEFAULT 0,
    storage_used_mb INTEGER NOT NULL DEFAULT 0,
    api_calls BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usage_metrics UNIQUE (organization_id, date),
    CONSTRAINT fk_usage_metrics_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_usage_metrics_organization_date
    ON usage_metrics(organization_id, date);

CREATE TABLE IF NOT EXISTS platform_audit_logs (
    log_id BIGSERIAL PRIMARY KEY,
    super_admin_id BIGINT,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255),
    entity_id BIGINT,
    organization_id BIGINT,
    details JSONB,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_platform_audit_super_admin
        FOREIGN KEY (super_admin_id) REFERENCES users(user_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_platform_audit_organization
        FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_platform_audit_super_admin
    ON platform_audit_logs(super_admin_id);
CREATE INDEX IF NOT EXISTS idx_platform_audit_action
    ON platform_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_platform_audit_entity_type
    ON platform_audit_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_platform_audit_timestamp
    ON platform_audit_logs(timestamp);
CREATE INDEX IF NOT EXISTS idx_platform_audit_organization
    ON platform_audit_logs(organization_id);
