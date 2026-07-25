package com.yonathan.featureflags.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.domain.FlagAuditEvent;
import com.yonathan.featureflags.repository.FlagAuditEventRepository;
import com.yonathan.featureflags.repository.FeatureFlagRepository;

class FeatureFlagManagementServiceTests {

	@Test
	void createsANewFlag() {
		TestFlagAuditEventRepository auditRepository = new TestFlagAuditEventRepository();
		FeatureFlagManagementService service = serviceWith(new TestFeatureFlagRepository(), auditRepository);

		FeatureFlag createdFlag = service.create("beta-search", true, 10, "yonathan");

		assertEquals("beta-search", createdFlag.key());
		assertEquals(10, createdFlag.rolloutPercentage());
		assertEquals(1, auditRepository.events.size());
		assertEquals("yonathan", auditRepository.events.getFirst().actor());
		assertEquals("FLAG_CREATED", auditRepository.events.getFirst().action());
	}

	@Test
	void rejectsADuplicateFlagKey() {
		TestFeatureFlagRepository repository = new TestFeatureFlagRepository();
		repository.save(new FeatureFlag("beta-search", true, 10));
		FeatureFlagManagementService service = serviceWith(repository, new TestFlagAuditEventRepository());

		assertThrows(
				FeatureFlagAlreadyExistsException.class,
				() -> service.create("beta-search", false, 50, "yonathan")
		);
	}

	@Test
	void updatesOnlyTheFieldsProvidedAndRecordsBothStates() {
		TestFeatureFlagRepository repository = new TestFeatureFlagRepository();
		repository.save(new FeatureFlag("beta-search", true, 10));
		TestFlagAuditEventRepository auditRepository = new TestFlagAuditEventRepository();
		FeatureFlagManagementService service = serviceWith(repository, auditRepository);

		FeatureFlag updatedFlag = service.update("beta-search", null, 60, "yonathan");

		assertTrue(updatedFlag.enabled());
		assertEquals(60, updatedFlag.rolloutPercentage());
		assertEquals("FLAG_UPDATED", auditRepository.events.getFirst().action());
		assertEquals(10, auditRepository.events.getFirst().previousState().rolloutPercentage());
		assertEquals(60, auditRepository.events.getFirst().newState().rolloutPercentage());
	}

	private FeatureFlagManagementService serviceWith(
			FeatureFlagRepository featureFlagRepository,
			FlagAuditEventRepository auditEventRepository
	) {
		return new FeatureFlagManagementService(
				featureFlagRepository,
				new FlagAuditService(auditEventRepository)
		);
	}

	private static class TestFeatureFlagRepository implements FeatureFlagRepository {

		private final Map<String, FeatureFlag> flags = new LinkedHashMap<>();

		@Override
		public Optional<FeatureFlag> findByKey(String key) {
			return Optional.ofNullable(flags.get(key));
		}

		@Override
		public List<FeatureFlag> findAll() {
			return new ArrayList<>(flags.values());
		}

		@Override
		public boolean save(FeatureFlag flag) {
			if (flags.containsKey(flag.key())) {
				return false;
			}

			flags.put(flag.key(), flag);
			return true;
		}

		@Override
		public boolean update(FeatureFlag flag) {
			if (!flags.containsKey(flag.key())) {
				return false;
			}

			flags.put(flag.key(), flag);
			return true;
		}
	}

	private static class TestFlagAuditEventRepository implements FlagAuditEventRepository {

		private final AtomicLong nextId = new AtomicLong(1);
		private final List<FlagAuditEvent> events = new ArrayList<>();

		@Override
		public FlagAuditEvent save(FlagAuditEvent event) {
			FlagAuditEvent savedEvent = new FlagAuditEvent(
					nextId.getAndIncrement(),
					event.flagKey(),
					event.action(),
					event.actor(),
					event.occurredAt(),
					event.previousState(),
					event.newState()
			);
			events.add(savedEvent);
			return savedEvent;
		}

		@Override
		public List<FlagAuditEvent> findByFlagKey(String flagKey) {
			return events.stream().filter(event -> event.flagKey().equals(flagKey)).toList();
		}
	}
}
