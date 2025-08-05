-- Database migration script to add version columns for optimistic locking
-- Run this script after the initial database setup

-- Connect to saga_db and add version column to saga_executions table
\c saga_db;
ALTER TABLE saga_executions ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Connect to inventory_db and add version column to stock_reservations table
\c inventory_db;
ALTER TABLE stock_reservations ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Create indexes for better performance on version columns
CREATE INDEX IF NOT EXISTS idx_saga_executions_version ON saga_executions(version);
CREATE INDEX IF NOT EXISTS idx_stock_reservations_version ON stock_reservations(version);

-- Update existing records to have version 0 if they don't have a version
UPDATE saga_executions SET version = 0 WHERE version IS NULL;
UPDATE stock_reservations SET version = 0 WHERE version IS NULL;

-- Make version columns NOT NULL after setting default values
ALTER TABLE saga_executions ALTER COLUMN version SET NOT NULL;
ALTER TABLE stock_reservations ALTER COLUMN version SET NOT NULL;