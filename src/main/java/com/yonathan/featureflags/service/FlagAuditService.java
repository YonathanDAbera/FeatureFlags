package com.yonathan.featureflags.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.domain.FlagAuditEvent;
import com.yonathan.featureflags.domain.FlagState;
import com.yonathan.featureflags.repository.FlagAuditEventRepository;

@Service
public class FlagAuditService {

	private final FlagAuditEventRepository flagAuditEventRepository;

	public FlagAuditService(FlagAuditEventRepository flagAuditEventRepository) {
		this.flagAuditEventRepository = flagAuditEventRepository;
	}

	public void recordCreated(FeatureFlag flag, String actor) {
		FlagAuditEvent event = new FlagAuditEvent(
				null,
				flag.key(),
				"FLAG_CREATED",
				actor,
				Instant.now(),
				null,
				new FlagState(flag.enabled(), flag.rolloutPercentage())
		);
		flagAuditEventRepository.save(event);
	}

	public void recordUpdated(FeatureFlag previousFlag, FeatureFlag updatedFlag, String actor) {
		FlagAuditEvent event = new FlagAuditEvent(
				null,
				updatedFlag.key(),
				"FLAG_UPDATED",
				actor,
				Instant.now(),
				new FlagState(previousFlag.enabled(), previousFlag.rolloutPercentage()),
				new FlagState(updatedFlag.enabled(), updatedFlag.rolloutPercentage())
		);
		flagAuditEventRepository.save(event);
	}

	public List<FlagAuditEvent> findByFlagKey(String flagKey) {
		return flagAuditEventRepository.findByFlagKey(flagKey);
	}
}
