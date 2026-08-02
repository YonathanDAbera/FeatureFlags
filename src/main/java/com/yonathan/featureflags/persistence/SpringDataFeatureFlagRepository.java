package com.yonathan.featureflags.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.yonathan.featureflags.domain.Environment;
import java.util.Optional;

public interface SpringDataFeatureFlagRepository extends JpaRepository<FeatureFlagEntity, Long> {

	Optional<FeatureFlagEntity> findByEnvironmentAndKey(Environment environment, String key);
	List<FeatureFlagEntity> findAllByEnvironmentOrderByKeyAsc(Environment environment);
}
