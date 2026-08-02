package com.yonathan.featureflags.repository;

import java.util.List;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.domain.TargetingRule;

public interface TargetingRuleRepository {
	List<TargetingRule> findByEnvironmentAndFlagKey(Environment environment, String flagKey);
	TargetingRule save(TargetingRule rule);
}
