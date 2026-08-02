package com.yonathan.featureflags.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.repository.FeatureFlagRepository;

@Service
public class FeatureFlagManagementService {

	private final FeatureFlagRepository featureFlagRepository;
	private final FlagAuditService flagAuditService;

	public FeatureFlagManagementService(
			FeatureFlagRepository featureFlagRepository,
			FlagAuditService flagAuditService
	) {
		this.featureFlagRepository = featureFlagRepository;
		this.flagAuditService = flagAuditService;
	}

	@Transactional
	@CacheEvict(cacheNames = "flag-evaluations", allEntries = true)
	public FeatureFlag create(Environment environment, String key, boolean enabled, int rolloutPercentage, String actor) {
		FeatureFlag flag = new FeatureFlag(environment, key, enabled, rolloutPercentage);

		if (!featureFlagRepository.save(flag)) {
			throw new FeatureFlagAlreadyExistsException(key);
		}

		flagAuditService.recordCreated(flag, actor);
		return flag;
	}

	public List<FeatureFlag> findAll(Environment environment) {
		return featureFlagRepository.findAllByEnvironment(environment);
	}

	@Transactional
	@CacheEvict(cacheNames = "flag-evaluations", allEntries = true)
	public FeatureFlag update(Environment environment, String key, Boolean enabled, Integer rolloutPercentage, String actor) {
		FeatureFlag previousFlag = featureFlagRepository.findByEnvironmentAndKey(environment, key)
				.orElseThrow(() -> new FeatureFlagNotFoundException(key));
		FeatureFlag updatedFlag = new FeatureFlag(
				environment, key,
				enabled == null ? previousFlag.enabled() : enabled,
				rolloutPercentage == null ? previousFlag.rolloutPercentage() : rolloutPercentage
		);

		if (!featureFlagRepository.update(updatedFlag)) {
			throw new FeatureFlagNotFoundException(key);
		}

		flagAuditService.recordUpdated(previousFlag, updatedFlag, actor);
		return updatedFlag;
	}
}
