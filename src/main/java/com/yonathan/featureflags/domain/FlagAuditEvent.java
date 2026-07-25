package com.yonathan.featureflags.domain;

import java.time.Instant;

public record FlagAuditEvent(
		Long id,
		String flagKey,
		String action,
		String actor,
		Instant occurredAt,
		FlagState previousState,
		FlagState newState
) {
}
