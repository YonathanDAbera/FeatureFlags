package com.yonathan.featureflags;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yonathan.featureflags.domain.FlagAuditEvent;
import com.yonathan.featureflags.service.FlagAuditService;

@RestController
@RequestMapping("/api/v1/flags")
public class FlagAuditController {

	private final FlagAuditService flagAuditService;

	public FlagAuditController(FlagAuditService flagAuditService) {
		this.flagAuditService = flagAuditService;
	}

	@GetMapping("/{flagKey}/audit-events")
	public List<FlagAuditEvent> findByFlagKey(@PathVariable String flagKey) {
		return flagAuditService.findByFlagKey(flagKey);
	}
}
