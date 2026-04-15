package com.sportspulse.gateway.services;

import com.sportspulse.gateway.config.properties.GatewayServicesProperties;
import com.sportspulse.gateway.dto.responses.HealthResponse;
import com.sportspulse.gateway.integration.client.ServiceClient;
import com.sportspulse.gateway.services.components.GatewayHealthIndicator;
import com.sportspulse.gateway.utils.enums.ServiceStatus;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ServiceHealthImpl Implementation of {@link HealthService} that aggregates the health status of
 * the gateway and its dependent microservices. Uses {@link ServiceClient} to query each service's
 * actuator health endpoint and determines the overall gateway status based on the availability of
 * all services.
 */
@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

  private final ServiceClient serviceClient;
  private final GatewayServicesProperties properties;
  private final GatewayHealthIndicator gatewayHealthIndicator;

  @Override
  public Mono<HealthResponse> getHeathStatusServices() {
    Map<String, String> servicesConfig =
        Map.of(
            "ms-auth", properties.getAuth(),
            "ms-leagues", properties.getLeagues(),
            "ms-teams", properties.getTeams(),
            "ms-fixtures", properties.getFixtures(),
            "ms-standings", properties.getStandings(),
            "ms-notifications", properties.getNotifications(),
            "ms-dashboard", properties.getDashboard());

    return Flux.fromIterable(servicesConfig.entrySet())
        .flatMap(
            entry ->
                serviceClient
                    .getHealthService(entry.getKey(), entry.getValue())
                    .map(status -> Map.entry(entry.getKey(), status)))
        .collectMap(Map.Entry::getKey, Map.Entry::getValue)
        .flatMap(
            servicesStatus -> {
              ServiceStatus dependenciesStatus =
                  servicesStatus.values().stream().allMatch(s -> s == ServiceStatus.UP)
                      ? ServiceStatus.UP
                      : ServiceStatus.DOWN;

              return gatewayHealthIndicator
                  .getGatewayStatus()
                  .map(
                      gatewayStatus ->
                          HealthResponse.builder()
                              .gateway(gatewayStatus)
                              .dependencies(dependenciesStatus)
                              .services(servicesStatus)
                              .build());
            });
  }
}
