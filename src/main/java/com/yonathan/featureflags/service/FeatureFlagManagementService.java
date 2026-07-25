package com.yonathan.featureflags.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.repository.FeatureFlagRepository;

@Service
public class FeatureFlagManagementService {

	private final FeatureFlagRepository featureFlagRepository;

	public FeatureFlagManagementService(FeatureFlagRepository featureFlagRepository) {
		this.featureFlagRepository = featureFlagRepository;
	}

	public FeatureFlag create(String key, boolean enabled, int rolloutPercentage) {
		FeatureFlag flag = new FeatureFlag(key, enabled, rolloutPercentage);

		if (!featureFlagRepository.save(flag)) {
			throw new FeatureFlagAlreadyExistsException(key);
		}

		return flag;
	}

	public List<FeatureFlag> findAll() {
		return featureFlagRepository.findAll();
	}
}
