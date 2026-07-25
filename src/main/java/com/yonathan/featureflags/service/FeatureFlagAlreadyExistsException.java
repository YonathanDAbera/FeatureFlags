package com.yonathan.featureflags.service;

public class FeatureFlagAlreadyExistsException extends RuntimeException {

	public FeatureFlagAlreadyExistsException(String key) {
		super("A flag with key '" + key + "' already exists");
	}
}
