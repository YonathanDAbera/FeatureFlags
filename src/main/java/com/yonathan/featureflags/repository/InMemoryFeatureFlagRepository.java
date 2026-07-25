package com.yonathan.featureflags.repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;

import com.yonathan.featureflags.domain.FeatureFlag;

@Repository
public class InMemoryFeatureFlagRepository implements FeatureFlagRepository {

	private final ConcurrentMap<String, FeatureFlag> flags = new ConcurrentHashMap<>();

	public InMemoryFeatureFlagRepository() {
		flags.put("new-checkout", new FeatureFlag("new-checkout", true, 25));
		flags.put("dark-mode", new FeatureFlag("dark-mode", true, 100));
		flags.put("legacy-dashboard", new FeatureFlag("legacy-dashboard", false, 100));
	}

	@Override
	public Optional<FeatureFlag> findByKey(String key) {
		return Optional.ofNullable(flags.get(key));
	}

	@Override
	public List<FeatureFlag> findAll() {
		return flags.values().stream()
				.sorted((first, second) -> first.key().compareTo(second.key()))
				.toList();
	}

	@Override
	public boolean save(FeatureFlag flag) {
		return flags.putIfAbsent(flag.key(), flag) == null;
	}
}
