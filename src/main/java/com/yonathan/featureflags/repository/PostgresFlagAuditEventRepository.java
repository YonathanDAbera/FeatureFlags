package com.yonathan.featureflags.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.yonathan.featureflags.domain.FlagAuditEvent;
import com.yonathan.featureflags.domain.FlagState;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.persistence.FlagAuditEventEntity;
import com.yonathan.featureflags.persistence.SpringDataFlagAuditEventRepository;

@Repository
public class PostgresFlagAuditEventRepository implements FlagAuditEventRepository {

	private final SpringDataFlagAuditEventRepository springDataRepository;

	public PostgresFlagAuditEventRepository(SpringDataFlagAuditEventRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public FlagAuditEvent save(FlagAuditEvent event) {
		return toDomain(springDataRepository.save(toEntity(event)));
	}

	@Override
	public List<FlagAuditEvent> findByEnvironmentAndFlagKey(Environment environment, String flagKey) {
		return springDataRepository.findByEnvironmentAndFlagKeyOrderByOccurredAtDescIdDesc(environment, flagKey).stream()
				.map(this::toDomain)
				.toList();
	}

	private FlagAuditEvent toDomain(FlagAuditEventEntity entity) {
		FlagState previousState = entity.getPreviousEnabled() == null
				? null
				: new FlagState(entity.getPreviousEnabled(), entity.getPreviousRolloutPercentage());
		FlagState newState = new FlagState(entity.isNewEnabled(), entity.getNewRolloutPercentage());

		return new FlagAuditEvent(
				entity.getId(),
				entity.getEnvironment(),
				entity.getFlagKey(),
				entity.getAction(),
				entity.getActor(),
				entity.getOccurredAt(),
				previousState,
				newState,
				entity.getDetails()
		);
	}

	private FlagAuditEventEntity toEntity(FlagAuditEvent event) {
		Boolean previousEnabled = event.previousState() == null ? null : event.previousState().enabled();
		Integer previousRolloutPercentage = event.previousState() == null
				? null
				: event.previousState().rolloutPercentage();

		return new FlagAuditEventEntity(
				event.environment(), event.flagKey(),
				event.action(),
				event.actor(),
				event.occurredAt(),
				previousEnabled,
				previousRolloutPercentage,
				event.newState().enabled(),
				event.newState().rolloutPercentage(),
				event.details()
		);
	}
}
