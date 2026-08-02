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
	private final FlagAuditService flagAuditService;
	public TargetingRuleService(TargetingRuleRepository targetingRuleRepository, FeatureFlagRepository featureFlagRepository, FlagAuditService flagAuditService) { this.targetingRuleRepository = targetingRuleRepository; this.featureFlagRepository = featureFlagRepository; this.flagAuditService = flagAuditService; }
	public List<TargetingRule> findAll(Environment environment, String flagKey) { return targetingRuleRepository.findByEnvironmentAndFlagKey(environment, flagKey); }
	@Transactional @CacheEvict(cacheNames = "flag-evaluations", allEntries = true)
	public TargetingRule create(Environment environment, String flagKey, String userId, int priority, String actor) {
		var flag = featureFlagRepository.findByEnvironmentAndKey(environment, flagKey).orElseThrow(() -> new FeatureFlagNotFoundException(flagKey));
		TargetingRule rule = targetingRuleRepository.save(new TargetingRule(null, environment, flagKey, userId, priority));
		flagAuditService.recordTargetingRuleChange(flag, "TARGETING_RULE_ADDED", userId, actor);
		return rule;
	}
	@Transactional @CacheEvict(cacheNames = "flag-evaluations", allEntries = true)
	public void delete(Environment environment, String flagKey, Long ruleId, String actor) {
		var rule = targetingRuleRepository.findByEnvironmentAndFlagKey(environment, flagKey).stream().filter(item -> item.id().equals(ruleId)).findFirst().orElseThrow(() -> new TargetingRuleNotFoundException(ruleId));
		var flag = featureFlagRepository.findByEnvironmentAndKey(environment, flagKey).orElseThrow(() -> new FeatureFlagNotFoundException(flagKey));
		if (!targetingRuleRepository.delete(ruleId, environment, flagKey)) throw new TargetingRuleNotFoundException(ruleId);
		flagAuditService.recordTargetingRuleChange(flag, "TARGETING_RULE_REMOVED", rule.userId(), actor);
	}
}
