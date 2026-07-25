package com.yonathan.featureflags.repository;

import java.util.List;
import java.util.Optional;

import com.yonathan.featureflags.domain.FeatureFlag;

public interface FeatureFlagRepository {

	Optional<FeatureFlag> findByKey(String key);

	List<FeatureFlag> findAll();

	boolean save(FeatureFlag flag);
}
