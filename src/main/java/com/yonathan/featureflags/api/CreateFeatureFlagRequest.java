package com.yonathan.featureflags.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateFeatureFlagRequest(
		@NotBlank(message = "key must not be blank") String key,
		boolean enabled,
		@Min(value = 0, message = "rolloutPercentage must be at least 0")
		@Max(value = 100, message = "rolloutPercentage must be at most 100")
		int rolloutPercentage
) {
}
