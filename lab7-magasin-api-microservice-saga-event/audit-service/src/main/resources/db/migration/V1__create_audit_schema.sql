-- Audit Service Database Schema
-- Creates tables for comprehensive audit logging and compliance tracking

-- Create audit_logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    correlation_id VARCHAR(36),
    causation_id VARCHAR(36),
    service_name VARCHAR(50) NOT NULL,
    event_data TEXT,
    metadata TEXT,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    processed_by VARCHAR(50) NOT NULL DEFAULT 'audit-service',
    audit_level VARCHAR(20) NOT NULL,
    business_context TEXT,
    compliance_tags VARCHAR(500),
    CONSTRAINT audit_level_check CHECK (audit_level IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW', 'INFO'))
);

-- Create indexes for efficient querying
CREATE INDEX idx_audit_event_type ON audit_logs(event_type);
CREATE INDEX idx_audit_aggregate_id ON audit_logs(aggregate_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_correlation_id ON audit_logs(correlation_id);
CREATE INDEX idx_audit_service_name ON audit_logs(service_name);
CREATE INDEX idx_audit_level ON audit_logs(audit_level);
CREATE INDEX idx_audit_processed_at ON audit_logs(processed_at DESC);
CREATE INDEX idx_audit_business_context ON audit_logs(business_context);

-- Composite indexes for common query patterns
CREATE INDEX idx_audit_service_type ON audit_logs(service_name, event_type);
CREATE INDEX idx_audit_level_timestamp ON audit_logs(audit_level, timestamp DESC);
CREATE INDEX idx_audit_correlation_timestamp ON audit_logs(correlation_id, timestamp ASC);

-- Create compliance_reports table for tracking compliance actions
CREATE TABLE compliance_reports (
    id BIGSERIAL PRIMARY KEY,
    audit_log_id BIGINT NOT NULL REFERENCES audit_logs(id),
    report_type VARCHAR(50) NOT NULL,
    report_data TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(50) NOT NULL DEFAULT 'audit-service',
    compliance_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT compliance_status_check CHECK (compliance_status IN ('PENDING', 'PROCESSED', 'ESCALATED', 'RESOLVED'))
);

CREATE INDEX idx_compliance_audit_log_id ON compliance_reports(audit_log_id);
CREATE INDEX idx_compliance_type ON compliance_reports(report_type);
CREATE INDEX idx_compliance_status ON compliance_reports(compliance_status);
CREATE INDEX idx_compliance_created_at ON compliance_reports(created_at DESC);

-- Create audit_metrics table for performance tracking
CREATE TABLE audit_metrics (
    id BIGSERIAL PRIMARY KEY,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    metric_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    metric_tags TEXT,
    service_name VARCHAR(50),
    event_type VARCHAR(100)
);

CREATE INDEX idx_metrics_name ON audit_metrics(metric_name);
CREATE INDEX idx_metrics_timestamp ON audit_metrics(metric_timestamp DESC);
CREATE INDEX idx_metrics_service ON audit_metrics(service_name);

-- Create a view for compliance dashboard
CREATE VIEW compliance_dashboard AS
SELECT 
    al.audit_level,
    al.service_name,
    al.event_type,
    COUNT(*) as event_count,
    COUNT(CASE WHEN al.audit_level = 'CRITICAL' THEN 1 END) as critical_count,
    COUNT(CASE WHEN al.audit_level = 'HIGH' THEN 1 END) as high_count,
    MIN(al.timestamp) as first_event,
    MAX(al.timestamp) as last_event
FROM audit_logs al
WHERE al.timestamp >= NOW() - INTERVAL '24 hours'
GROUP BY al.audit_level, al.service_name, al.event_type
ORDER BY critical_count DESC, high_count DESC, event_count DESC;

-- Create a view for recent critical events
CREATE VIEW recent_critical_events AS
SELECT 
    al.event_id,
    al.event_type,
    al.aggregate_id,
    al.service_name,
    al.timestamp,
    al.correlation_id,
    al.business_context,
    al.compliance_tags
FROM audit_logs al
WHERE al.audit_level = 'CRITICAL'
    AND al.timestamp >= NOW() - INTERVAL '7 days'
ORDER BY al.timestamp DESC;

-- Comments for documentation
COMMENT ON TABLE audit_logs IS 'Comprehensive audit log for all business events in the system';
COMMENT ON COLUMN audit_logs.event_id IS 'Unique identifier for the business event';
COMMENT ON COLUMN audit_logs.correlation_id IS 'Correlation ID for tracing related events';
COMMENT ON COLUMN audit_logs.audit_level IS 'Audit importance level: CRITICAL, HIGH, MEDIUM, LOW, INFO';
COMMENT ON COLUMN audit_logs.business_context IS 'Business domain context for the event';
COMMENT ON COLUMN audit_logs.compliance_tags IS 'Comma-separated compliance tags for categorization';

COMMENT ON TABLE compliance_reports IS 'Compliance reporting and tracking for audit events';
COMMENT ON TABLE audit_metrics IS 'Performance metrics for audit service monitoring';

COMMENT ON VIEW compliance_dashboard IS 'Dashboard view for compliance monitoring and reporting';
COMMENT ON VIEW recent_critical_events IS 'Recent critical events requiring immediate attention';
