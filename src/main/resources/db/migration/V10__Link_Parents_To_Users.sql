ALTER TABLE parents ADD COLUMN IF NOT EXISTS user_id BIGINT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_parents_user_id
    ON parents(user_id) WHERE user_id IS NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_parents_user') THEN
        ALTER TABLE parents ADD CONSTRAINT fk_parents_user
            FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL;
    END IF;
END $$;
