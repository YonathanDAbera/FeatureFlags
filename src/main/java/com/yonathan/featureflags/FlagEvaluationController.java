package com.yonathan.featureflags;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yonathan.featureflags.domain.FlagEvaluationResult;
import com.yonathan.featureflags.service.FlagEvaluationService;

@RestController
@RequestMapping("/api/v1/flags")
public class FlagEvaluationController {

	private final FlagEvaluationService flagEvaluationService;

	public FlagEvaluationController(FlagEvaluationService flagEvaluationService) {
		this.flagEvaluationService = flagEvaluationService;
	}

	@GetMapping("/{flagKey}/evaluate")
	public FlagEvaluationResult evaluate(
			@PathVariable String flagKey,
			@RequestParam String userId
	) {
		return flagEvaluationService.evaluate(flagKey, userId);
	}
}
