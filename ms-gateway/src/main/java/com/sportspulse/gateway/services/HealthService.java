package com.sportspulse.gateway.services;

import com.sportspulse.gateway.dto.responses.HealthResponse;
import reactor.core.publisher.Mono;

/**
 * ServiceHealth Defines the contract for retrieving the overall health status of the gateway and
 * its dependent services.
 */
public interface HealthService {

  /**
   * Retrieves the aggregated health status of all monitored services.
   *
   * @return a HealthResponse containing gateway and service statuses
   */
  Mono<HealthResponse> getHeathStatusServices();
}
