package com.yonathan.featureflags.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.repository.FeatureFlagRepository;
import com.yonathan.featureflags.repository.TargetingRuleRepository;

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

		TargetingRuleRepository targetingRules = new TargetingRuleRepository() {
			public List<com.yonathan.featureflags.domain.TargetingRule> findByEnvironmentAndFlagKey(Environment environment, String key) { return List.of(); }
			public com.yonathan.featureflags.domain.TargetingRule save(com.yonathan.featureflags.domain.TargetingRule rule) { return rule; }
		};

		var result = new FlagEvaluationService(repository, new SimpleMeterRegistry(), targetingRules)
				.evaluate(Environment.staging, "new-checkout", "user-123");

		assertTrue(result.enabled());
		assertEquals(Environment.staging, result.environment());
	}
}
