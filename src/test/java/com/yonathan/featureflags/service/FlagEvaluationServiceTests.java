package com.yonathan.featureflags.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.repository.FeatureFlagRepository;

class FlagEvaluationServiceTests {

	@Test
	void evaluatesAFullRolloutWithinItsEnvironment() {
		FeatureFlagRepository repository = new FeatureFlagRepository() {
			public Optional<FeatureFlag> findByEnvironmentAndKey(Environment environment, String key) {
				return Optional.of(new FeatureFlag(environment, key, true, 100));
			}
			public List<FeatureFlag> findAllByEnvironment(Environment environment) { return List.of(); }
			public boolean save(FeatureFlag flag) { return false; }
			public boolean update(FeatureFlag flag) { return false; }
		};

		var result = new FlagEvaluationService(repository).evaluate(Environment.staging, "new-checkout", "user-123");

		assertTrue(result.enabled());
		assertEquals(Environment.staging, result.environment());
	}
}
