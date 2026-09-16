CREATE TABLE diagnostic_session_analysis (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES diagnostic_session(id),
    version INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    correlation_id UUID NOT NULL,
    request_key VARCHAR(200) NOT NULL,
    title_snapshot VARCHAR(160) NOT NULL,
    scenario_snapshot TEXT NOT NULL,
    result_json JSONB,
    error_code VARCHAR(80),
    error_detail TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_diagnostic_analysis_session_version UNIQUE (session_id, version),
    CONSTRAINT uq_diagnostic_analysis_session_request_key UNIQUE (session_id, request_key),
    CONSTRAINT chk_diagnostic_analysis_status
        CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX idx_diagnostic_analysis_session_version
    ON diagnostic_session_analysis (session_id, version DESC);

CREATE INDEX idx_diagnostic_analysis_correlation_id
    ON diagnostic_session_analysis (correlation_id);
