package com.yonathan.featureflags.domain;

public record FlagState(boolean enabled, int rolloutPercentage) {
}
