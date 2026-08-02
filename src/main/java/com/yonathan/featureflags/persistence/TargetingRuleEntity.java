package com.yonathan.featureflags.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "feature_flag_targeting_rules")
public class TargetingRuleEntity {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
	@Enumerated(EnumType.STRING) @Column(nullable = false) private com.yonathan.featureflags.domain.Environment environment;
	@Column(name = "flag_key", nullable = false) private String flagKey;
	@Column(name = "user_id", nullable = false) private String userId;
	@Column(nullable = false) private int priority;

	protected TargetingRuleEntity() { }
	public TargetingRuleEntity(com.yonathan.featureflags.domain.Environment environment, String flagKey, String userId, int priority) {
		this.environment = environment; this.flagKey = flagKey; this.userId = userId; this.priority = priority;
	}
	public Long getId() { return id; }
	public com.yonathan.featureflags.domain.Environment getEnvironment() { return environment; }
	public String getFlagKey() { return flagKey; }
	public String getUserId() { return userId; }
	public int getPriority() { return priority; }
}
