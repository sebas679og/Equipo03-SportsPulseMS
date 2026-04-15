package com.sportspulse.gateway.dto.responses;

import com.sportspulse.gateway.utils.enums.ServiceStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/**
 * HealthResponse Represents the health status of the gateway and its dependent services. Contains
 * the gateway status, a timestamp of the health check, and a map of individual service statuses.
 */
@Value
@Builder
public class HealthResponse {
  ServiceStatus gateway;
  ServiceStatus dependencies;

  @Builder.Default Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);

  Map<String, ServiceStatus> services;
}
