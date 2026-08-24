-- Keep database constraints aligned with CreateSubjectRequest and Subject entity.
UPDATE subjects
SET code = 'SUBJ_' || subject_id
WHERE code IS NULL OR TRIM(code) = '';

UPDATE subjects
SET max_marks = 100
WHERE max_marks IS NULL;

ALTER TABLE subjects ALTER COLUMN code SET NOT NULL;
ALTER TABLE subjects ALTER COLUMN max_marks SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_subjects_org_code
    ON subjects (organization_id, code);
