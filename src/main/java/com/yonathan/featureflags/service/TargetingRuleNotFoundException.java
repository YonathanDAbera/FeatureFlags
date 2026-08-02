package com.yonathan.featureflags.service;

public class TargetingRuleNotFoundException extends RuntimeException {
	public TargetingRuleNotFoundException(Long id) { super("Targeting rule not found: " + id); }
}
