CREATE TABLE flag_audit_events (
    id BIGSERIAL PRIMARY KEY,
    flag_key VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    previous_enabled BOOLEAN,
    previous_rollout_percentage INTEGER,
    new_enabled BOOLEAN NOT NULL,
    new_rollout_percentage INTEGER NOT NULL CHECK (new_rollout_percentage BETWEEN 0 AND 100)
);

CREATE INDEX idx_flag_audit_events_flag_key_occurred_at
    ON flag_audit_events (flag_key, occurred_at DESC, id DESC);
