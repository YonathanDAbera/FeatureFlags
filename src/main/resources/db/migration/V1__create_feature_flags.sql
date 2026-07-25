CREATE TABLE feature_flags (
    flag_key VARCHAR(100) PRIMARY KEY,
    enabled BOOLEAN NOT NULL,
    rollout_percentage INTEGER NOT NULL CHECK (rollout_percentage BETWEEN 0 AND 100)
);

INSERT INTO feature_flags (flag_key, enabled, rollout_percentage) VALUES
    ('new-checkout', TRUE, 25),
    ('dark-mode', TRUE, 100),
    ('legacy-dashboard', FALSE, 100);
