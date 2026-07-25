package com.yonathan.featureflags.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "feature_flags")
public class FeatureFlagEntity {

	@Id
	@Column(name = "flag_key", nullable = false, updatable = false)
	private String key;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "rollout_percentage", nullable = false)
	private int rolloutPercentage;

	protected FeatureFlagEntity() {
	}

	public FeatureFlagEntity(String key, boolean enabled, int rolloutPercentage) {
		this.key = key;
		this.enabled = enabled;
		this.rolloutPercentage = rolloutPercentage;
	}

	public String getKey() {
		return key;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getRolloutPercentage() {
		return rolloutPercentage;
	}
}
