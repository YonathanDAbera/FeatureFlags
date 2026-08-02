package com.yonathan.featureflags.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTargetingRuleRequest(@NotBlank String userId, @Min(0) @Max(1000) int priority) {
}
