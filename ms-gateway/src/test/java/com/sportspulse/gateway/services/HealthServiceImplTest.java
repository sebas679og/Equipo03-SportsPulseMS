package com.sportspulse.gateway.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.sportspulse.gateway.config.properties.GatewayServicesProperties;
import com.sportspulse.gateway.integration.client.ServiceClient;
import com.sportspulse.gateway.services.components.GatewayHealthIndicator;
import com.sportspulse.gateway.utils.enums.ServiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("HealthServiceImpl Tests")
class HealthServiceImplTest {

  @Mock private ServiceClient serviceClient;
  @Mock private GatewayServicesProperties properties;
  @Mock private GatewayHealthIndicator gatewayHealthIndicator;

  @InjectMocks private HealthServiceImpl healthService;

  // -------------------------------------------------------------------------
  // Common stubs — properties always return a URL
  // -------------------------------------------------------------------------

  @BeforeEach
  void stubProperties() {
    when(properties.getAuth()).thenReturn("http://ms-auth:8080");
    when(properties.getLeagues()).thenReturn("http://ms-leagues:8080");
    when(properties.getTeams()).thenReturn("http://ms-teams:8080");
    when(properties.getFixtures()).thenReturn("http://ms-fixtures:8080");
    when(properties.getStandings()).thenReturn("http://ms-standings:8080");
    when(properties.getNotifications()).thenReturn("http://ms-notifications:8080");
    when(properties.getDashboard()).thenReturn("http://ms-dashboard:8080");
  }

  // Convenience: stub all 7 services with the same status
  private void stubAllServicesWith(ServiceStatus status) {
    when(serviceClient.getHealthService(anyString(), anyString())).thenReturn(Mono.just(status));
  }

  // =========================================================================
  // getHeathStatusServices()
  // =========================================================================

  @Nested
  @DisplayName("gateway status")
  class GatewayStatus {

    @Test
    @DisplayName("should set gateway field from GatewayHealthIndicator when it returns UP")
    void shouldSetGatewayUp_whenIndicatorReturnsUp() {
      stubAllServicesWith(ServiceStatus.UP);
      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.UP));

      StepVerifier.create(healthService.getHeathStatusServices())
          .assertNext(r -> assertThat(r.getGateway()).isEqualTo(ServiceStatus.UP))
          .verifyComplete();
    }

    @Test
    @DisplayName("should set gateway field from GatewayHealthIndicator when it returns DOWN")
    void shouldSetGatewayDown_whenIndicatorReturnsDown() {
      stubAllServicesWith(ServiceStatus.UP);
      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.DOWN));

      StepVerifier.create(healthService.getHeathStatusServices())
          .assertNext(r -> assertThat(r.getGateway()).isEqualTo(ServiceStatus.DOWN))
          .verifyComplete();
    }
  }

  // =========================================================================

  @Nested
  @DisplayName("dependencies aggregation")
  class DependenciesAggregation {

    @Test
    @DisplayName("should set dependencies UP when all services are UP")
    void shouldSetDependenciesUp_whenAllServicesAreUp() {
      stubAllServicesWith(ServiceStatus.UP);
      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.UP));

      StepVerifier.create(healthService.getHeathStatusServices())
          .assertNext(r -> assertThat(r.getDependencies()).isEqualTo(ServiceStatus.UP))
          .verifyComplete();
    }

    @Test
    @DisplayName("should set dependencies DOWN when at least one service is DOWN")
    void shouldSetDependenciesDown_whenAtLeastOneServiceIsDown() {
      // 6 UP, 1 DOWN
      when(serviceClient.getHealthService(anyString(), anyString()))
          .thenReturn(Mono.just(ServiceStatus.UP));
      when(serviceClient.getHealthService("ms-auth", "http://ms-auth:8080"))
          .thenReturn(Mono.just(ServiceStatus.DOWN));

      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.UP));

      StepVerifier.create(healthService.getHeathStatusServices())
          .assertNext(r -> assertThat(r.getDependencies()).isEqualTo(ServiceStatus.DOWN))
          .verifyComplete();
    }

    @Test
    @DisplayName("should set dependencies DOWN when all services are DOWN")
    void shouldSetDependenciesDown_whenAllServicesAreDown() {
      stubAllServicesWith(ServiceStatus.DOWN);
      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.UP));

      StepVerifier.create(healthService.getHeathStatusServices())
          .assertNext(r -> assertThat(r.getDependencies()).isEqualTo(ServiceStatus.DOWN))
          .verifyComplete();
    }
  }

  // =========================================================================

  @Nested
  @DisplayName("services map")
  class ServicesMap {

    @Test
    @DisplayName("should include all 7 service keys in the response map")
    void shouldIncludeAllSevenServiceKeys() {
      stubAllServicesWith(ServiceStatus.UP);
      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.UP));

      StepVerifier.create(healthService.getHeathStatusServices())
          .assertNext(
              r ->
                  assertThat(r.getServices())
                      .containsKeys(
                          "ms-auth",
                          "ms-leagues",
                          "ms-teams",
                          "ms-fixtures",
                          "ms-standings",
                          "ms-notifications",
                          "ms-dashboard"))
          .verifyComplete();
    }

    @Test
    @DisplayName("should reflect individual service statuses in the map")
    void shouldReflectIndividualStatusesInMap() {
      when(serviceClient.getHealthService(anyString(), anyString()))
          .thenReturn(Mono.just(ServiceStatus.UP));
      when(serviceClient.getHealthService("ms-fixtures", "http://ms-fixtures:8080"))
          .thenReturn(Mono.just(ServiceStatus.DOWN));

      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.UP));

      StepVerifier.create(healthService.getHeathStatusServices())
          .assertNext(
              r -> {
                assertThat(r.getServices()).containsEntry("ms-fixtures", ServiceStatus.DOWN);
                assertThat(r.getServices()).containsEntry("ms-auth", ServiceStatus.UP);
              })
          .verifyComplete();
    }
  }

  // =========================================================================

  @Nested
  @DisplayName("response structure")
  class ResponseStructure {

    @Test
    @DisplayName("should emit exactly one HealthResponse and complete")
    void shouldEmitOneResponseAndComplete() {
      stubAllServicesWith(ServiceStatus.UP);
      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.UP));

      StepVerifier.create(healthService.getHeathStatusServices())
          .expectNextCount(1)
          .verifyComplete();
    }

    @Test
    @DisplayName("should build a fully populated HealthResponse")
    void shouldBuildFullyPopulatedResponse() {
      stubAllServicesWith(ServiceStatus.UP);
      when(gatewayHealthIndicator.getGatewayStatus()).thenReturn(Mono.just(ServiceStatus.UP));

      StepVerifier.create(healthService.getHeathStatusServices())
          .assertNext(
              r -> {
                assertThat(r.getGateway()).isNotNull();
                assertThat(r.getDependencies()).isNotNull();
                assertThat(r.getServices()).isNotNull().hasSize(7);
              })
          .verifyComplete();
    }
  }
}
