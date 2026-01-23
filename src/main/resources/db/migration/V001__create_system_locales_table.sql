-- Migration V001: Create system_locales table
-- Description: Create the system_locales table to store system locale information
-- Author: Matheus Cruz

CREATE TABLE system_locales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(5) NOT NULL UNIQUE,
    language VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NULL
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_system_locales_code ON system_locales (code);
CREATE INDEX IF NOT EXISTS idx_system_locales_language ON system_locales (language);
CREATE INDEX IF NOT EXISTS idx_system_locales_country ON system_locales (country);
CREATE INDEX IF NOT EXISTS idx_system_locales_active ON system_locales (active);
CREATE INDEX IF NOT EXISTS idx_system_locales_created_at ON system_locales (created_at);
CREATE INDEX IF NOT EXISTS idx_system_locales_updated_at ON system_locales (updated_at);

-- Comments
COMMENT ON TABLE system_locales IS 'Table to store system locale information';
COMMENT ON COLUMN system_locales.id IS 'Unique identifier for the system locale';
COMMENT ON COLUMN system_locales.code IS 'Unique locale code (e.g., pt-BR, en-US)';
COMMENT ON COLUMN system_locales.language IS 'Language name (e.g., Portuguese, English)';
COMMENT ON COLUMN system_locales.country IS 'Country name';
COMMENT ON COLUMN system_locales.active IS 'Flag to indicate if the system locale is active';
COMMENT ON COLUMN system_locales.created_at IS 'Timestamp when the system locale was created';
COMMENT ON COLUMN system_locales.updated_at IS 'Timestamp when the system locale was updated';
