package com.sportspulse.gateway.services;

import com.sportspulse.gateway.dto.responses.HealthResponse;

/**
 * ServiceHealth Defines the contract for retrieving the overall health status of the gateway and
 * its dependent services.
 */
public interface ServiceHealth {

  /**
   * Retrieves the aggregated health status of all monitored services.
   *
   * @return a HealthResponse containing gateway and service statuses
   */
  HealthResponse getHeathStatusServices();
}
