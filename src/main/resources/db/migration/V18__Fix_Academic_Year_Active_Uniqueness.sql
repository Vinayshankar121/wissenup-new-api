-- Allow multiple inactive/closed academic years per organization while enforcing
-- the business rule that at most one academic year can be active.
ALTER TABLE academic_years
    DROP CONSTRAINT IF EXISTS uq_academic_years_org_active;

DROP INDEX IF EXISTS uq_academic_years_one_active;

CREATE UNIQUE INDEX uq_academic_years_one_active
    ON academic_years (organization_id)
    WHERE is_active = true;
