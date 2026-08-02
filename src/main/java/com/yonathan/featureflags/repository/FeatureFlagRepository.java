package com.yonathan.featureflags.repository;

import java.util.List;
import java.util.Optional;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.domain.Environment;

public interface FeatureFlagRepository {

	Optional<FeatureFlag> findByEnvironmentAndKey(Environment environment, String key);

	List<FeatureFlag> findAllByEnvironment(Environment environment);

	boolean save(FeatureFlag flag);

	boolean update(FeatureFlag flag);
}
