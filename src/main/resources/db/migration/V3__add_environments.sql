ALTER TABLE feature_flags ADD COLUMN id BIGSERIAL;
ALTER TABLE feature_flags ADD COLUMN environment VARCHAR(20) NOT NULL DEFAULT 'development';
ALTER TABLE feature_flags DROP CONSTRAINT feature_flags_pkey;
ALTER TABLE feature_flags ADD PRIMARY KEY (id);
ALTER TABLE feature_flags ADD CONSTRAINT uq_feature_flags_environment_key UNIQUE (environment, flag_key);

ALTER TABLE flag_audit_events ADD COLUMN environment VARCHAR(20) NOT NULL DEFAULT 'development';
CREATE INDEX idx_flag_audit_events_environment_key_occurred_at
    ON flag_audit_events (environment, flag_key, occurred_at DESC, id DESC);
