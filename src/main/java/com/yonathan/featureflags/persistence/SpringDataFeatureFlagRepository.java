package com.yonathan.featureflags.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFeatureFlagRepository extends JpaRepository<FeatureFlagEntity, String> {

	List<FeatureFlagEntity> findAllByOrderByKeyAsc();
}
