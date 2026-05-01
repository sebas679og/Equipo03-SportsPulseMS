package com.sportspulse.gateway.integration.client;

import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.integration.dtos.responses.ServiceResponse;
import com.sportspulse.gateway.utils.enums.ServiceStatus;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * ServiceClientImpl Implementation of {@link ServiceClient} that uses {@link WebClient} to check
 * the health status of external services via their actuator endpoints. Provides error handling and
 * logging to mark services as DOWN when requests fail, time out, or return invalid responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceClientImpl implements ServiceClient {

  private final WebClient webClient;

  @Override
  public Mono<ServiceStatus> getHealthService(String serviceName, String baseUrl) {

    return webClient
        .get()
        .uri(baseUrl + ApiPathsServices.Health.ACTUATOR_HEALTH)
        .retrieve()
        .bodyToMono(ServiceResponse.class)
        .map(response -> mapToStatus(serviceName, baseUrl, response))
        .timeout(Duration.ofSeconds(2))
        .onErrorResume(
            e -> {
              if (log.isErrorEnabled()) {
                log.error(
                    "Service [{}] DOWN - URL: {} - Error: {}",
                    serviceName,
                    baseUrl,
                    e.getMessage());
              }
              return Mono.just(ServiceStatus.DOWN);
            });
  }

  private ServiceStatus mapToStatus(String serviceName, String baseUrl, ServiceResponse response) {

    if (response == null || response.getStatus() == null) {
      if (log.isErrorEnabled()) {
        log.error("Service [{}] DOWN - URL: {} - Reason: empty or null body", serviceName, baseUrl);
      }
      return ServiceStatus.DOWN;
    }

    if (!"UP".equalsIgnoreCase(response.getStatus().name())) {
      if (log.isErrorEnabled()) {
        log.error(
            "Service [{}] DOWN - URL: {} - Actuator status: {}",
            serviceName,
            baseUrl,
            response.getStatus());
      }
      return ServiceStatus.DOWN;
    }

    return ServiceStatus.UP;
  }
}
