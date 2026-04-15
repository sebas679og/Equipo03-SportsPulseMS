package com.sportspulse.gateway.services;

import com.sportspulse.gateway.config.properties.GatewayServicesProperties;
import com.sportspulse.gateway.dto.responses.HealthResponse;
import com.sportspulse.gateway.integration.client.ServiceClient;
import com.sportspulse.gateway.utils.enums.ServiceStatus;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ServiceHealthImpl Implementation of {@link ServiceHealth} that aggregates the health status of
 * the gateway and its dependent microservices. Uses {@link ServiceClient} to query each service's
 * actuator health endpoint and determines the overall gateway status based on the availability of
 * all services.
 */
@Service
@RequiredArgsConstructor
public class ServiceHealthImpl implements ServiceHealth {

  private final ServiceClient serviceClient;
  private final GatewayServicesProperties properties;

  @Override
  public HealthResponse getHeathStatusServices() {
    Map<String, String> servicesConfig =
        Map.of(
            "ms-auth", properties.getAuth(),
            "ms-leagues", properties.getLeagues(),
            "ms-teams", properties.getTeams(),
            "ms-fixtures", properties.getFixtures(),
            "ms-standings", properties.getStandings(),
            "ms-notifications", properties.getNotifications(),
            "ms-dashboard", properties.getDashboard());

    Map<String, ServiceStatus> servicesStatus =
        servicesConfig.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> serviceClient.getHealthService(entry.getKey(), entry.getValue())));

    ServiceStatus gatewayStatus =
        servicesStatus.values().stream().allMatch(status -> status == ServiceStatus.UP)
            ? ServiceStatus.UP
            : ServiceStatus.DOWN;

    return HealthResponse.builder().gateway(gatewayStatus).services(servicesStatus).build();
  }
}
