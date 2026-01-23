-- Migration V005: Insert initial system locales values
-- Description: Insert the initial system locales (pt-BR and en-US) values
-- Author: Matheus Cruz

INSERT INTO system_locales (code, language, country, active, created_at) VALUES 
('pt-BR', 'Portuguese', 'Brazil', TRUE, CURRENT_TIMESTAMP), 
('en-US', 'English', 'United States', TRUE, CURRENT_TIMESTAMP);