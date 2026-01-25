-- Migration V006: Create tokens table
-- Description: Create the tokens table to store token information for email confirmation and other purposes
-- Author: Matheus Cruz

CREATE TABLE tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ NULL,
    is_consumed BOOLEAN NOT NULL DEFAULT FALSE,
    metadata JSONB NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_tokens_token ON tokens (token);
CREATE INDEX IF NOT EXISTS idx_tokens_token_type ON tokens (token_type);
CREATE INDEX IF NOT EXISTS idx_tokens_expires_at ON tokens (expires_at);
CREATE INDEX IF NOT EXISTS idx_tokens_is_consumed ON tokens (is_consumed);
CREATE INDEX IF NOT EXISTS idx_tokens_metadata_gin ON tokens USING GIN (metadata);
CREATE INDEX IF NOT EXISTS idx_tokens_created_at ON tokens (created_at);
CREATE INDEX IF NOT EXISTS idx_tokens_cleanup ON tokens (is_consumed, expires_at);

-- Comments
COMMENT ON TABLE tokens IS 'Table to store token information for email confirmation and other purposes';
COMMENT ON COLUMN tokens.id IS 'Unique identifier for the token';
COMMENT ON COLUMN tokens.token IS 'The token string value';
COMMENT ON COLUMN tokens.token_type IS 'Type of token (EMAIL_CONFIRMATION, PASSWORD_RESET, etc.)';
COMMENT ON COLUMN tokens.expires_at IS 'Timestamp when the token expires';
COMMENT ON COLUMN tokens.consumed_at IS 'Timestamp when the token was consumed';
COMMENT ON COLUMN tokens.is_consumed IS 'Flag to indicate if the token has been consumed';
COMMENT ON COLUMN tokens.metadata IS 'JSON metadata for token-specific information (e.g., email for EMAIL_CONFIRMATION tokens)';
COMMENT ON COLUMN tokens.created_at IS 'Timestamp when the token was created';
COMMENT ON COLUMN tokens.updated_at IS 'Timestamp when the token was updated';