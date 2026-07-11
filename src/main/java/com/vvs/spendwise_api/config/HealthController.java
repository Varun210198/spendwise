package com.vvs.spendwise_api.config;

import com.vvs.spendwise_api.common.response.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("UP", "SpendWise API");
	}
}
