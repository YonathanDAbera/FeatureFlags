package com.yonathan.featureflags.api;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateFeatureFlagRequest(
		Boolean enabled,
		@Min(value = 0, message = "rolloutPercentage must be at least 0")
		@Max(value = 100, message = "rolloutPercentage must be at most 100")
		Integer rolloutPercentage
) {

	@AssertTrue(message = "at least one field must be provided")
	public boolean hasUpdate() {
		return enabled != null || rolloutPercentage != null;
	}
}
