package com.yonathan.featureflags.domain;

public record FlagEvaluationResult(
		String flagKey,
		String userId,
		boolean enabled,
		int rolloutPercentage,
		String reason
) {
}
