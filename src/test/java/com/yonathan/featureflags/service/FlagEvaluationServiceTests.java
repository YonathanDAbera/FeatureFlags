package com.yonathan.featureflags.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.domain.FlagEvaluationResult;
import com.yonathan.featureflags.repository.FeatureFlagRepository;

class FlagEvaluationServiceTests {

	@Test
	void returnsDisabledForAnUnknownFlag() {
		FlagEvaluationService service = serviceWith(flagKey -> Optional.empty());

		FlagEvaluationResult result = service.evaluate("does-not-exist", "user-123");

		assertFalse(result.enabled());
		assertEquals("FLAG_NOT_FOUND", result.reason());
	}

	@Test
	void returnsDisabledWhenTheFlagIsDisabled() {
		FeatureFlag disabledFlag = new FeatureFlag("legacy-dashboard", false, 100);
		FlagEvaluationService service = serviceWith(flagKey -> Optional.of(disabledFlag));

		FlagEvaluationResult result = service.evaluate("legacy-dashboard", "user-123");

		assertFalse(result.enabled());
		assertEquals("FLAG_DISABLED", result.reason());
	}

	@Test
	void includesEveryUserInAFullRollout() {
		FeatureFlag fullRolloutFlag = new FeatureFlag("dark-mode", true, 100);
		FlagEvaluationService service = serviceWith(flagKey -> Optional.of(fullRolloutFlag));

		FlagEvaluationResult result = service.evaluate("dark-mode", "user-123");

		assertTrue(result.enabled());
		assertEquals("ROLLOUT_INCLUDED", result.reason());
	}

	@Test
	void returnsTheSameDecisionForTheSameUserAndFlag() {
		FeatureFlag partialRolloutFlag = new FeatureFlag("new-checkout", true, 25);
		FlagEvaluationService service = serviceWith(flagKey -> Optional.of(partialRolloutFlag));

		FlagEvaluationResult firstResult = service.evaluate("new-checkout", "user-123");
		FlagEvaluationResult secondResult = service.evaluate("new-checkout", "user-123");

		assertEquals(firstResult.enabled(), secondResult.enabled());
		assertEquals(firstResult.reason(), secondResult.reason());
	}

	private FlagEvaluationService serviceWith(FeatureFlagRepository repository) {
		return new FlagEvaluationService(repository);
	}
}
