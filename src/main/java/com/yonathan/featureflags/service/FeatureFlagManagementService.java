package com.yonathan.featureflags.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yonathan.featureflags.domain.FeatureFlag;
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
	public FeatureFlag create(String key, boolean enabled, int rolloutPercentage, String actor) {
		FeatureFlag flag = new FeatureFlag(key, enabled, rolloutPercentage);

		if (!featureFlagRepository.save(flag)) {
			throw new FeatureFlagAlreadyExistsException(key);
		}

		flagAuditService.recordCreated(flag, actor);
		return flag;
	}

	public List<FeatureFlag> findAll() {
		return featureFlagRepository.findAll();
	}
}
