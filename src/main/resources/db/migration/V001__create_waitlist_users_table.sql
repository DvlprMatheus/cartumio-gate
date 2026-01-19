-- Migration V001: Create waitlist_users table
-- Description: Create the waitlist_users table to store waitlist user information
-- Author: Matheus Cruz

CREATE TABLE waitlist_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    is_confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_waitlist_users_email ON waitlist_users (email);
CREATE INDEX IF NOT EXISTS idx_waitlist_users_created_at ON waitlist_users (created_at);
CREATE INDEX IF NOT EXISTS idx_waitlist_users_updated_at ON waitlist_users (updated_at);
CREATE INDEX IF NOT EXISTS idx_waitlist_users_is_confirmed ON waitlist_users (is_confirmed);
CREATE INDEX IF NOT EXISTS idx_waitlist_users_first_name ON waitlist_users (first_name);
CREATE INDEX IF NOT EXISTS idx_waitlist_users_last_name ON waitlist_users (last_name);

-- Comments
COMMENT ON TABLE waitlist_users IS 'Table to store waitlist user information';
COMMENT ON COLUMN waitlist_users.id IS 'Unique identifier for the waitlist user';
COMMENT ON COLUMN waitlist_users.first_name IS 'First name of the waitlist user';
COMMENT ON COLUMN waitlist_users.last_name IS 'Last name of the waitlist user';
COMMENT ON COLUMN waitlist_users.email IS 'Email of the waitlist user';
COMMENT ON COLUMN waitlist_users.is_confirmed IS 'Flag to indicate if the waitlist user is confirmed';
COMMENT ON COLUMN waitlist_users.created_at IS 'Timestamp when the waitlist user was created';
COMMENT ON COLUMN waitlist_users.updated_at IS 'Timestamp when the waitlist user was updated';