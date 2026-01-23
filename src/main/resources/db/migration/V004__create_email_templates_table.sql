-- Migration V004: Create email_templates table
-- Description: Create the email_templates table to store email template information
-- Author: Matheus Cruz

CREATE TABLE email_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    language VARCHAR(5) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_email_templates_code ON email_templates (code);
CREATE INDEX IF NOT EXISTS idx_email_templates_language ON email_templates (language);
CREATE INDEX IF NOT EXISTS idx_email_templates_active ON email_templates (active);
CREATE INDEX IF NOT EXISTS idx_email_templates_created_at ON email_templates (created_at);
CREATE INDEX IF NOT EXISTS idx_email_templates_updated_at ON email_templates (updated_at);

-- Unique constraint: code + language must be unique for active templates
CREATE UNIQUE INDEX IF NOT EXISTS idx_email_templates_code_language_unique_active 
    ON email_templates (code, language) 
    WHERE active = true;

-- Comments
COMMENT ON TABLE email_templates IS 'Table to store email template information';
COMMENT ON COLUMN email_templates.id IS 'Unique identifier for the email template';
COMMENT ON COLUMN email_templates.code IS 'Code identifier for the email template';
COMMENT ON COLUMN email_templates.language IS 'Language code for the email template (e.g., pt-BR, en-US)';
COMMENT ON COLUMN email_templates.subject IS 'Subject of the email template';
COMMENT ON COLUMN email_templates.body IS 'Body content of the email template';
COMMENT ON COLUMN email_templates.active IS 'Flag to indicate if the email template is active';
COMMENT ON COLUMN email_templates.created_at IS 'Timestamp when the email template was created';
COMMENT ON COLUMN email_templates.updated_at IS 'Timestamp when the email template was updated';

-- Constraint comment
COMMENT ON INDEX idx_email_templates_code_language_unique_active IS 'Ensures that the combination of code and language is unique for active email templates';
