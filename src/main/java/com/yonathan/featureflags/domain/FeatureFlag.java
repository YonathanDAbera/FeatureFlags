package com.yonathan.featureflags.domain;

public record FeatureFlag(String key, boolean enabled, int rolloutPercentage) {

	public FeatureFlag {
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("Flag key must not be blank");
		}

		if (rolloutPercentage < 0 || rolloutPercentage > 100) {
			throw new IllegalArgumentException("Rollout percentage must be between 0 and 100");
		}
	}
}
