package com.yonathan.featureflags.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.repository.FeatureFlagRepository;

class FeatureFlagManagementServiceTests {

	@Test
	void createsANewFlag() {
		FeatureFlagManagementService service = new FeatureFlagManagementService(new TestFeatureFlagRepository());

		FeatureFlag createdFlag = service.create("beta-search", true, 10);

		assertEquals("beta-search", createdFlag.key());
		assertEquals(10, createdFlag.rolloutPercentage());
	}

	@Test
	void rejectsADuplicateFlagKey() {
		TestFeatureFlagRepository repository = new TestFeatureFlagRepository();
		repository.save(new FeatureFlag("beta-search", true, 10));
		FeatureFlagManagementService service = new FeatureFlagManagementService(repository);

		assertThrows(
				FeatureFlagAlreadyExistsException.class,
				() -> service.create("beta-search", false, 50)
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
	}
}
