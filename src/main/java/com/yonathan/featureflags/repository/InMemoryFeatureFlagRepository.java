package com.yonathan.featureflags.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.yonathan.featureflags.domain.FeatureFlag;

@Repository
public class InMemoryFeatureFlagRepository implements FeatureFlagRepository {

	private final Map<String, FeatureFlag> flags = Map.of(
			"new-checkout", new FeatureFlag("new-checkout", true, 25),
			"dark-mode", new FeatureFlag("dark-mode", true, 100),
			"legacy-dashboard", new FeatureFlag("legacy-dashboard", false, 100)
	);

	@Override
	public Optional<FeatureFlag> findByKey(String key) {
		return Optional.ofNullable(flags.get(key));
	}
}
