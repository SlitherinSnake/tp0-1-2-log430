-- Event Store Database Schema Migration V1
-- Creates the events table with proper indexing for Event Sourcing

-- Create events table with all required columns and constraints
CREATE TABLE events (
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
    CONSTRAINT unique_aggregate_version UNIQUE(aggregate_id, event_version)
);

-- Create indexes for optimal query performance
CREATE INDEX idx_events_aggregate ON events(aggregate_id);
CREATE INDEX idx_events_type ON events(event_type);
CREATE INDEX idx_events_timestamp ON events(timestamp);
CREATE INDEX idx_events_correlation ON events(correlation_id);
CREATE INDEX idx_events_aggregate_type ON events(aggregate_type);
CREATE INDEX idx_events_causation ON events(causation_id);
CREATE INDEX idx_events_aggregate_version ON events(aggregate_id, event_version);
CREATE INDEX idx_events_replay ON events(aggregate_id, event_version, timestamp);

-- Create a sequence for event ordering
CREATE SEQUENCE event_sequence START 1;