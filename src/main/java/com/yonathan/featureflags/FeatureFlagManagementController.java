package com.yonathan.featureflags;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yonathan.featureflags.api.CreateFeatureFlagRequest;
import com.yonathan.featureflags.api.UpdateFeatureFlagRequest;
import com.yonathan.featureflags.domain.FeatureFlag;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.service.FeatureFlagManagementService;

@RestController
@RequestMapping("/api/v1/environments/{environment}/flags")
public class FeatureFlagManagementController {

	private final FeatureFlagManagementService featureFlagManagementService;

	public FeatureFlagManagementController(FeatureFlagManagementService featureFlagManagementService) {
		this.featureFlagManagementService = featureFlagManagementService;
	}

	@GetMapping
	public List<FeatureFlag> findAll(@PathVariable Environment environment) {
		return featureFlagManagementService.findAll(environment);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FeatureFlag create(
			@PathVariable Environment environment,
			@Valid @RequestBody CreateFeatureFlagRequest request,
			@RequestHeader(name = "X-Actor", defaultValue = "system") String actor
	) {
		return featureFlagManagementService.create(
				environment, request.key(),
				request.enabled(),
				request.rolloutPercentage(),
				actor
		);
	}

	@PatchMapping("/{flagKey}")
	public FeatureFlag update(
			@PathVariable Environment environment,
			@PathVariable String flagKey,
			@Valid @RequestBody UpdateFeatureFlagRequest request,
			@RequestHeader(name = "X-Actor", defaultValue = "system") String actor
	) {
		return featureFlagManagementService.update(
				environment, flagKey,
				request.enabled(),
				request.rolloutPercentage(),
				actor
		);
	}
}
