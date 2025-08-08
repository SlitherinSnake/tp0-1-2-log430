-- Event Store Database Schema Migration Script
-- This script creates the events table with proper indexing for the Event Store

-- Connect to event_store_db
\c event_store_db;

-- Create events table with all required columns and constraints
CREATE TABLE IF NOT EXISTS events (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_version INTEGER NOT NULL,
    event_data JSONB NOT NULL,
    metadata JSONB,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    correlation_id UUID,
    causation_id UUID,
    UNIQUE(aggregate_id, event_version)
);

-- Create indexes for optimal query performance
CREATE INDEX IF NOT EXISTS idx_events_aggregate ON events(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_events_type ON events(event_type);
CREATE INDEX IF NOT EXISTS idx_events_timestamp ON events(timestamp);
CREATE INDEX IF NOT EXISTS idx_events_correlation ON events(correlation_id);

-- Create additional indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_events_aggregate_type ON events(aggregate_type);
CREATE INDEX IF NOT EXISTS idx_events_causation ON events(causation_id);
CREATE INDEX IF NOT EXISTS idx_events_aggregate_version ON events(aggregate_id, event_version);

-- Create a composite index for event replay queries
CREATE INDEX IF NOT EXISTS idx_events_replay ON events(aggregate_id, event_version, timestamp);

-- Add comments for documentation
COMMENT ON TABLE events IS 'Event Store table for storing all domain events with event sourcing capabilities';
COMMENT ON COLUMN events.event_id IS 'Unique identifier for each event';
COMMENT ON COLUMN events.event_type IS 'Type of the domain event (e.g., TransactionCreated, PaymentProcessed)';
COMMENT ON COLUMN events.aggregate_id IS 'Identifier of the aggregate that generated the event';
COMMENT ON COLUMN events.aggregate_type IS 'Type of the aggregate (e.g., Transaction, Payment, Inventory)';
COMMENT ON COLUMN events.event_version IS 'Version number for optimistic concurrency control';
COMMENT ON COLUMN events.event_data IS 'JSON payload containing the event data';
COMMENT ON COLUMN events.metadata IS 'Additional metadata for the event';
COMMENT ON COLUMN events.timestamp IS 'When the event was created';
COMMENT ON COLUMN events.correlation_id IS 'Correlation ID for tracing related events';
COMMENT ON COLUMN events.causation_id IS 'ID of the event that caused this event';

-- Create a sequence for event ordering (optional, can be used for global ordering)
CREATE SEQUENCE IF NOT EXISTS event_sequence START 1;

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON TABLE events TO magasin;
GRANT ALL PRIVILEGES ON SEQUENCE event_sequence TO magasin;