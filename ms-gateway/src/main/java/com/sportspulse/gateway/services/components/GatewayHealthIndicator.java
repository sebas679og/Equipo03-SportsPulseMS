package com.sportspulse.gateway.services.components;

import com.sportspulse.gateway.utils.enums.ServiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * GatewayHealthIndicator Reactive health indicator for the gateway application. Implements {@link
 * ReactiveHealthIndicator} to expose the gateway health status and provides a method to map it into
 * a {@link ServiceStatus}.
 */
@Component
@RequiredArgsConstructor
public class GatewayHealthIndicator implements ReactiveHealthIndicator {

  @Override
  public Mono<Health> health() {
    return Mono.just(Health.up().build());
  }

  /**
   * Retrieves the gateway status as a {@link ServiceStatus}. Maps the actuator health status to UP
   * or DOWN and falls back to DOWN on errors.
   *
   * @return a Mono emitting the gateway {@link ServiceStatus}
   */
  public Mono<ServiceStatus> getGatewayStatus() {
    return health()
        .map(
            h ->
                "UP".equalsIgnoreCase(h.getStatus().getCode())
                    ? ServiceStatus.UP
                    : ServiceStatus.DOWN)
        .onErrorReturn(ServiceStatus.DOWN);
  }
}
