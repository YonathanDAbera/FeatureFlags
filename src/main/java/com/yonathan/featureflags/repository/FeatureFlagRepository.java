package com.yonathan.featureflags.repository;

import java.util.Optional;

import com.yonathan.featureflags.domain.FeatureFlag;

public interface FeatureFlagRepository {

	Optional<FeatureFlag> findByKey(String key);
}
