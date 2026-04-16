package com.sportspulse.gateway.controller;

import com.sportspulse.gateway.config.constants.ApiPaths;
import com.sportspulse.gateway.dto.responses.HealthResponse;
import com.sportspulse.gateway.services.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * HealthController REST controller that exposes the health check endpoint. Delegates to {@link
 * HealthService} to retrieve the aggregated health status of the gateway and its dependent
 * services.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.HEALTH)
public class HealthController {

  private final HealthService healthService;

  @GetMapping
  public Mono<HealthResponse> getHealthServices() {
    return healthService.getHeathStatusServices();
  }
}
