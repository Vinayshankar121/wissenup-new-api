ALTER TABLE schools
    ADD COLUMN IF NOT EXISTS organization_type VARCHAR(50) NOT NULL DEFAULT 'SCHOOL',
    ADD COLUMN IF NOT EXISTS registration_number VARCHAR(100);

CREATE UNIQUE INDEX IF NOT EXISTS idx_schools_registration_number
    ON schools(registration_number)
    WHERE registration_number IS NOT NULL;
