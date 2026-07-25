package com.yonathan.featureflags.repository;

import java.util.List;

import com.yonathan.featureflags.domain.FlagAuditEvent;

public interface FlagAuditEventRepository {

	FlagAuditEvent save(FlagAuditEvent event);

	List<FlagAuditEvent> findByFlagKey(String flagKey);
}
