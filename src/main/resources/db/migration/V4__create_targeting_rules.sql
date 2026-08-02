CREATE TABLE feature_flag_targeting_rules (
    id BIGSERIAL PRIMARY KEY,
    environment VARCHAR(32) NOT NULL,
    flag_key VARCHAR(100) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_targeting_rule UNIQUE (environment, flag_key, user_id)
);

CREATE INDEX idx_targeting_rules_lookup
    ON feature_flag_targeting_rules (environment, flag_key, priority, id);
