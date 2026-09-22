ALTER TABLE users
ADD COLUMN payvia_handle VARCHAR(100);

-- Backfill handles for existing users (using lowercased, alphanumerics only + @payvia)
-- A simplistic safe approach for PostgreSQL since we can't easily iterate and check duplicates cleanly in pure basic SQL without procedures.
-- Assuming no current collisions in 'full_name' simple parsing. If there are, it will fail on the UNIQUE constraint, which is desired.
UPDATE users 
SET payvia_handle = CONCAT(
    REGEXP_REPLACE(LOWER(full_name), '[^a-z0-9_]', '', 'g'), 
    '@payvia'
)
WHERE payvia_handle IS NULL;

-- Enforce constraints
ALTER TABLE users 
ALTER COLUMN payvia_handle SET NOT NULL,
ADD CONSTRAINT uc_payvia_handle UNIQUE (payvia_handle);

CREATE INDEX idx_users_payvia_handle ON users(payvia_handle);
