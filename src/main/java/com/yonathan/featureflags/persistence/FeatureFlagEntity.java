package com.yonathan.featureflags.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "feature_flags")
public class FeatureFlagEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private com.yonathan.featureflags.domain.Environment environment;

	@Column(name = "flag_key", nullable = false, updatable = false)
	private String key;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "rollout_percentage", nullable = false)
	private int rolloutPercentage;

	protected FeatureFlagEntity() {
	}

	public FeatureFlagEntity(com.yonathan.featureflags.domain.Environment environment, String key, boolean enabled, int rolloutPercentage) {
		this.environment = environment;
		this.key = key;
		this.enabled = enabled;
		this.rolloutPercentage = rolloutPercentage;
	}

	public com.yonathan.featureflags.domain.Environment getEnvironment() { return environment; }

	public String getKey() {
		return key;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getRolloutPercentage() {
		return rolloutPercentage;
	}

	public void update(boolean enabled, int rolloutPercentage) {
		this.enabled = enabled;
		this.rolloutPercentage = rolloutPercentage;
	}
}
