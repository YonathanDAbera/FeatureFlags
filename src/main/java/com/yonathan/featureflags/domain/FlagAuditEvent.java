package com.yonathan.featureflags.domain;

import java.time.Instant;

public record FlagAuditEvent(
		Long id,
		Environment environment,
		String flagKey,
		String action,
		String actor,
		Instant occurredAt,
		FlagState previousState,
		FlagState newState,
		String details
) {
}
