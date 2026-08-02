package com.yonathan.featureflags.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTargetingRuleRepository extends JpaRepository<TargetingRuleEntity, Long> {
	List<TargetingRuleEntity> findByEnvironmentAndFlagKeyOrderByPriorityAscIdAsc(com.yonathan.featureflags.domain.Environment environment, String flagKey);
	Optional<TargetingRuleEntity> findByIdAndEnvironmentAndFlagKey(Long id, com.yonathan.featureflags.domain.Environment environment, String flagKey);
}
