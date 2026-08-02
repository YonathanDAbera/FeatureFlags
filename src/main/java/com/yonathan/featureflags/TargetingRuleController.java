package com.yonathan.featureflags;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.yonathan.featureflags.api.CreateTargetingRuleRequest;
import com.yonathan.featureflags.domain.Environment;
import com.yonathan.featureflags.domain.TargetingRule;
import com.yonathan.featureflags.service.TargetingRuleService;

@RestController
@RequestMapping("/api/v1/environments/{environment}/flags/{flagKey}/targeting-rules")
public class TargetingRuleController {
	private final TargetingRuleService service;
	public TargetingRuleController(TargetingRuleService service) { this.service = service; }
	@GetMapping public List<TargetingRule> findAll(@PathVariable Environment environment, @PathVariable String flagKey) { return service.findAll(environment, flagKey); }
	@PostMapping @ResponseStatus(HttpStatus.CREATED) public TargetingRule create(@PathVariable Environment environment, @PathVariable String flagKey, @Valid @RequestBody CreateTargetingRuleRequest request, @RequestHeader(name = "X-Actor", defaultValue = "system") String actor) { return service.create(environment, flagKey, request.userId(), request.priority(), actor); }
	@DeleteMapping("/{ruleId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Environment environment, @PathVariable String flagKey, @PathVariable Long ruleId, @RequestHeader(name = "X-Actor", defaultValue = "system") String actor) { service.delete(environment, flagKey, ruleId, actor); }
}
