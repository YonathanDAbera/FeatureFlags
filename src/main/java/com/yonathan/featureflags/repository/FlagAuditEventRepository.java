package com.yonathan.featureflags.repository;

import java.util.List;

import com.yonathan.featureflags.domain.FlagAuditEvent;
import com.yonathan.featureflags.domain.Environment;

public interface FlagAuditEventRepository {

	FlagAuditEvent save(FlagAuditEvent event);

	List<FlagAuditEvent> findByEnvironmentAndFlagKey(Environment environment, String flagKey);
}
