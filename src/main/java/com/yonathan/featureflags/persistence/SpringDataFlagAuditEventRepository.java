package com.yonathan.featureflags.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.yonathan.featureflags.domain.Environment;

public interface SpringDataFlagAuditEventRepository extends JpaRepository<FlagAuditEventEntity, Long> {

	List<FlagAuditEventEntity> findByEnvironmentAndFlagKeyOrderByOccurredAtDescIdDesc(Environment environment, String flagKey);
}
