ALTER TABLE diagnostic_session_analysis
    ADD COLUMN request_id UUID;

UPDATE diagnostic_session_analysis
SET request_id = id
WHERE request_id IS NULL;

ALTER TABLE diagnostic_session_analysis
    ALTER COLUMN request_id SET NOT NULL;

CREATE INDEX idx_diagnostic_analysis_request_id
    ON diagnostic_session_analysis (request_id);
