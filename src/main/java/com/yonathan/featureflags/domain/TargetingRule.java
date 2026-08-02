package com.yonathan.featureflags.domain;

public record TargetingRule(Long id, Environment environment, String flagKey, String userId, int priority) {
}
