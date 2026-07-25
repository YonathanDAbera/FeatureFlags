package com.yonathan.featureflags.service;

public class FeatureFlagNotFoundException extends RuntimeException {

	public FeatureFlagNotFoundException(String key) {
		super("No flag exists with key '" + key + "'");
	}
}
