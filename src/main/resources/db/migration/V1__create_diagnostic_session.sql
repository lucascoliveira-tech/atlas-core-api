CREATE TABLE diagnostic_session (
    id UUID PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    scenario TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_diagnostic_session_created_at
    ON diagnostic_session (created_at DESC);

