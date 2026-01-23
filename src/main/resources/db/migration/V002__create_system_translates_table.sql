-- Migration V002: Create system_translates table
-- Description: Create the system_translates table to store system translation information
-- Author: Matheus Cruz

CREATE TABLE system_translates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(50) NOT NULL,
    value VARCHAR(255) NOT NULL,
    system_locale_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NULL,
    CONSTRAINT fk_system_translates_system_locale FOREIGN KEY (system_locale_id) REFERENCES system_locales(id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_system_translates_key ON system_translates (key);
CREATE INDEX IF NOT EXISTS idx_system_translates_system_locale_id ON system_translates (system_locale_id);
CREATE INDEX IF NOT EXISTS idx_system_translates_created_at ON system_translates (created_at);
CREATE INDEX IF NOT EXISTS idx_system_translates_updated_at ON system_translates (updated_at);

-- Comments
COMMENT ON TABLE system_translates IS 'Table to store system translation information';
COMMENT ON COLUMN system_translates.id IS 'Unique identifier for the system translation';
COMMENT ON COLUMN system_translates.key IS 'Translation key identifier';
COMMENT ON COLUMN system_translates.value IS 'Translated value';
COMMENT ON COLUMN system_translates.system_locale_id IS 'Foreign key reference to system_locales table';
COMMENT ON COLUMN system_translates.created_at IS 'Timestamp when the system translation was created';
COMMENT ON COLUMN system_translates.updated_at IS 'Timestamp when the system translation was updated';
