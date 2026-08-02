package com.yonathan.featureflags.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.persistence.FeatureFlagEntity;
import com.yonathan.featureflags.persistence.SpringDataFeatureFlagRepository;

@Repository
public class PostgresFeatureFlagRepository implements FeatureFlagRepository {

	private final SpringDataFeatureFlagRepository springDataRepository;

	public PostgresFeatureFlagRepository(SpringDataFeatureFlagRepository springDataRepository) {
		this.springDataRepository = springDataRepository;
	}

	@Override
	public Optional<FeatureFlag> findByEnvironmentAndKey(Environment environment, String key) {
		return springDataRepository.findByEnvironmentAndKey(environment, key).map(this::toDomain);
	}

	@Override
	public List<FeatureFlag> findAllByEnvironment(Environment environment) {
		return springDataRepository.findAllByEnvironmentOrderByKeyAsc(environment).stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public boolean save(FeatureFlag flag) {
		if (springDataRepository.findByEnvironmentAndKey(flag.environment(), flag.key()).isPresent()) {
			return false;
		}

		try {
			springDataRepository.saveAndFlush(toEntity(flag));
			return true;
		} catch (DataIntegrityViolationException exception) {
			return false;
		}
	}

	@Override
	public boolean update(FeatureFlag flag) {
		return springDataRepository.findByEnvironmentAndKey(flag.environment(), flag.key())
				.map(entity -> {
					entity.update(flag.enabled(), flag.rolloutPercentage());
					springDataRepository.saveAndFlush(entity);
					return true;
				})
				.orElse(false);
	}

	private FeatureFlag toDomain(FeatureFlagEntity entity) {
		return new FeatureFlag(entity.getEnvironment(), entity.getKey(), entity.isEnabled(), entity.getRolloutPercentage());
	}

	private FeatureFlagEntity toEntity(FeatureFlag flag) {
		return new FeatureFlagEntity(flag.environment(), flag.key(), flag.enabled(), flag.rolloutPercentage());
	}
}
