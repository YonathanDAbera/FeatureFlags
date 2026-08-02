package com.yonathan.featureflags.service;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.domain.TargetingRule;
import com.yonathan.featureflags.repository.FeatureFlagRepository;
import com.yonathan.featureflags.repository.TargetingRuleRepository;

@Service
public class TargetingRuleService {
	private final TargetingRuleRepository targetingRuleRepository;
	private final FeatureFlagRepository featureFlagRepository;
	public TargetingRuleService(TargetingRuleRepository targetingRuleRepository, FeatureFlagRepository featureFlagRepository) { this.targetingRuleRepository = targetingRuleRepository; this.featureFlagRepository = featureFlagRepository; }
	public List<TargetingRule> findAll(Environment environment, String flagKey) { return targetingRuleRepository.findByEnvironmentAndFlagKey(environment, flagKey); }
	@Transactional @CacheEvict(cacheNames = "flag-evaluations", allEntries = true)
	public TargetingRule create(Environment environment, String flagKey, String userId, int priority) {
		if (featureFlagRepository.findByEnvironmentAndKey(environment, flagKey).isEmpty()) throw new FeatureFlagNotFoundException(flagKey);
		return targetingRuleRepository.save(new TargetingRule(null, environment, flagKey, userId, priority));
	}
}
