package com.yonathan.featureflags.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.persistence.FeatureFlagEntity;
import com.yonathan.featureflags.persistence.SpringDataFeatureFlagRepository;

@Repository
public class PostgresFeatureFlagRepository implements FeatureFlagRepository {

	private final SpringDataFeatureFlagRepository springDataRepository;

	public PostgresFeatureFlagRepository(SpringDataFeatureFlagRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public Optional<FeatureFlag> findByKey(String key) {
		return springDataRepository.findById(key).map(this::toDomain);
	}

	@Override
	public List<FeatureFlag> findAll() {
		return springDataRepository.findAllByOrderByKeyAsc().stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public boolean save(FeatureFlag flag) {
		if (springDataRepository.existsById(flag.key())) {
			return false;
		}

		try {
			springDataRepository.saveAndFlush(toEntity(flag));
			return true;
		} catch (DataIntegrityViolationException exception) {
			return false;
		}
	}

	private FeatureFlag toDomain(FeatureFlagEntity entity) {
		return new FeatureFlag(entity.getKey(), entity.isEnabled(), entity.getRolloutPercentage());
	}

	private FeatureFlagEntity toEntity(FeatureFlag flag) {
		return new FeatureFlagEntity(flag.key(), flag.enabled(), flag.rolloutPercentage());
	}
}
