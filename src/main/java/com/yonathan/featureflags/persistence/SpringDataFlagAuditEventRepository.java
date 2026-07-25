package com.yonathan.featureflags.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataFlagAuditEventRepository extends JpaRepository<FlagAuditEventEntity, Long> {

	List<FlagAuditEventEntity> findByFlagKeyOrderByOccurredAtDescIdDesc(String flagKey);
}
