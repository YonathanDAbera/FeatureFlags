package com.yonathan.featureflags.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.domain.TargetingRule;
import com.yonathan.featureflags.persistence.SpringDataTargetingRuleRepository;
import com.yonathan.featureflags.persistence.TargetingRuleEntity;

@Repository
public class PostgresTargetingRuleRepository implements TargetingRuleRepository {
	private final SpringDataTargetingRuleRepository repository;
	public PostgresTargetingRuleRepository(SpringDataTargetingRuleRepository repository) { this.repository = repository; }
	public List<TargetingRule> findByEnvironmentAndFlagKey(Environment environment, String flagKey) {
		return repository.findByEnvironmentAndFlagKeyOrderByPriorityAscIdAsc(environment, flagKey).stream().map(this::toDomain).toList();
	}
	public TargetingRule save(TargetingRule rule) { return toDomain(repository.save(new TargetingRuleEntity(rule.environment(), rule.flagKey(), rule.userId(), rule.priority()))); }
	public boolean delete(Long id, Environment environment, String flagKey) {
		return repository.findByIdAndEnvironmentAndFlagKey(id, environment, flagKey)
				.map(rule -> { repository.delete(rule); return true; })
				.orElse(false);
	}
	private TargetingRule toDomain(TargetingRuleEntity entity) { return new TargetingRule(entity.getId(), entity.getEnvironment(), entity.getFlagKey(), entity.getUserId(), entity.getPriority()); }
}
