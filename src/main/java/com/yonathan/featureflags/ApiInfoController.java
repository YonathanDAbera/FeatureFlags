package com.yonathan.featureflags;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1")
public class ApiInfoController {

	@GetMapping("/info")
	public Map<String, String> info() {
		return Map.of(
				"service", "feature-flag-trials",
				"status", "ok"
		);
	}
}
