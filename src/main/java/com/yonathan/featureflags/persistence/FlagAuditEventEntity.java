package com.yonathan.featureflags.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "flag_audit_events")
public class FlagAuditEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private com.yonathan.featureflags.domain.Environment environment;

	@Column(name = "flag_key", nullable = false, updatable = false)
	private String flagKey;

	@Column(nullable = false, updatable = false)
	private String action;

	@Column(nullable = false, updatable = false)
	private String actor;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	@Column(name = "previous_enabled", updatable = false)
	private Boolean previousEnabled;

	@Column(name = "previous_rollout_percentage", updatable = false)
	private Integer previousRolloutPercentage;

	@Column(name = "new_enabled", updatable = false)
	private boolean newEnabled;

	@Column(name = "new_rollout_percentage", nullable = false, updatable = false)
	private int newRolloutPercentage;

	@Column(updatable = false)
	private String details;

	protected FlagAuditEventEntity() {
	}

	public FlagAuditEventEntity(
			com.yonathan.featureflags.domain.Environment environment, String flagKey,
			String action,
			String actor,
			Instant occurredAt,
			Boolean previousEnabled,
			Integer previousRolloutPercentage,
			boolean newEnabled,
			int newRolloutPercentage,
			String details
	) {
		this.environment = environment; this.flagKey = flagKey;
		this.action = action;
		this.actor = actor;
		this.occurredAt = occurredAt;
		this.previousEnabled = previousEnabled;
		this.previousRolloutPercentage = previousRolloutPercentage;
		this.newEnabled = newEnabled;
		this.newRolloutPercentage = newRolloutPercentage;
		this.details = details;
	}

	public Long getId() {
		return id;
	}
	public com.yonathan.featureflags.domain.Environment getEnvironment() { return environment; }

	public String getFlagKey() {
		return flagKey;
	}

	public String getAction() {
		return action;
	}

	public String getActor() {
		return actor;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public Boolean getPreviousEnabled() {
		return previousEnabled;
	}

	public Integer getPreviousRolloutPercentage() {
		return previousRolloutPercentage;
	}

	public boolean isNewEnabled() {
		return newEnabled;
	}

	public int getNewRolloutPercentage() {
		return newRolloutPercentage;
	}
	public String getDetails() { return details; }
}
