package com.yonathan.featureflags.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.domain.FlagEvaluationResult;
import com.yonathan.featureflags.repository.FeatureFlagRepository;

@Service
public class FlagEvaluationService {

	private final FeatureFlagRepository featureFlagRepository;
	private final MeterRegistry meterRegistry;

	public FlagEvaluationService(FeatureFlagRepository featureFlagRepository, MeterRegistry meterRegistry) {
		this.featureFlagRepository = featureFlagRepository;
		this.meterRegistry = meterRegistry;
	}

	@Cacheable(cacheNames = "flag-evaluations", key = "#environment.name() + ':' + #flagKey + ':' + #userId")
	public FlagEvaluationResult evaluate(Environment environment, String flagKey, String userId) {
		FeatureFlag flag = featureFlagRepository.findByEnvironmentAndKey(environment, flagKey).orElse(null);

		if (flag == null) {
			return record(new FlagEvaluationResult(environment, flagKey, userId, false, 0, "FLAG_NOT_FOUND"));
		}

		if (!flag.enabled()) {
			return record(new FlagEvaluationResult(environment, flagKey, userId, false, flag.rolloutPercentage(), "FLAG_DISABLED"));
		}

		boolean includedInRollout = stableBucket(environment, flagKey, userId) < flag.rolloutPercentage();
		String reason = includedInRollout ? "ROLLOUT_INCLUDED" : "ROLLOUT_EXCLUDED";

		return record(new FlagEvaluationResult(
				environment, flagKey,
				userId,
				includedInRollout,
				flag.rolloutPercentage(),
				reason
		));
	}

	private FlagEvaluationResult record(FlagEvaluationResult result) {
		meterRegistry.counter(
				"featureflags.evaluations",
				"environment", result.environment().name(),
				"decision", result.reason()
		).increment();
		return result;
	}

	private int stableBucket(Environment environment, String flagKey, String userId) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest((environment + ":" + flagKey + ":" + userId).getBytes(StandardCharsets.UTF_8));
			int firstFourBytes = ((hash[0] & 0xff) << 24)
					| ((hash[1] & 0xff) << 16)
					| ((hash[2] & 0xff) << 8)
					| (hash[3] & 0xff);
			return Math.floorMod(firstFourBytes, 100);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 should be available in every Java runtime", exception);
		}
	}
}
