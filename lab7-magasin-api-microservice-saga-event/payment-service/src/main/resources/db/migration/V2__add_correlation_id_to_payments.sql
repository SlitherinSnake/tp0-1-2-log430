-- Add correlation_id column to payments table for event correlation tracking
ALTER TABLE payments ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(255);

-- Create index on correlation_id for efficient querying
CREATE INDEX IF NOT EXISTS idx_payments_correlation_id ON payments(correlation_id);

-- Update existing payments with a default correlation_id (UUID)
UPDATE payments 
SET correlation_id = gen_random_uuid()::text 
WHERE correlation_id IS NULL;