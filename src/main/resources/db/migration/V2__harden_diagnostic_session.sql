ALTER TABLE diagnostic_session
    ADD CONSTRAINT chk_diagnostic_session_status
    CHECK (status IN ('DRAFT', 'ANALYZING', 'COMPLETED', 'FAILED'));

CREATE INDEX idx_diagnostic_session_created_at_id
    ON diagnostic_session (created_at DESC, id DESC);
