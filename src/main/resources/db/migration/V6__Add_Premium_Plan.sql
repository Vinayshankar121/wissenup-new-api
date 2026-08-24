ALTER TABLE subscription_plans
    ADD COLUMN IF NOT EXISTS yearly_price NUMERIC(19,2),
    ADD COLUMN IF NOT EXISTS duration_days INTEGER NOT NULL DEFAULT 365,
    ADD COLUMN IF NOT EXISTS grace_period_days INTEGER NOT NULL DEFAULT 7;

CREATE TABLE IF NOT EXISTS plan_modules (
    plan_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    PRIMARY KEY (plan_id, module_id),
    CONSTRAINT fk_plan_modules_plan FOREIGN KEY (plan_id)
        REFERENCES subscription_plans(plan_id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_modules_module FOREIGN KEY (module_id)
        REFERENCES modules(module_id) ON DELETE RESTRICT
);

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

INSERT INTO plan_modules (plan_id, module_id)
SELECT plan.plan_id, module.module_id
FROM subscription_plans plan
CROSS JOIN modules module
WHERE plan.code = 'PREMIUM' AND module.is_active = TRUE
ON CONFLICT (plan_id, module_id) DO NOTHING;
