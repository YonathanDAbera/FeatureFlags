package com.yonathan.featureflags.domain;

public record FlagEvaluationResult(
		Environment environment,
		String flagKey,
		String userId,
		boolean enabled,
		int rolloutPercentage,
		String reason
) {
}
