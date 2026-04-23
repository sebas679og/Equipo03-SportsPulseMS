package com.sportspulse.gateway.services.components;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sportspulse.gateway.utils.enums.ServiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("GatewayHealthIndicator Tests")
class GatewayHealthIndicatorTest {

  private GatewayHealthIndicator healthIndicator;

  @BeforeEach
  void setUp() {
    healthIndicator = new GatewayHealthIndicator();
  }

  // -------------------------------------------------------------------------
  // health()
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("health() should emit Health.up()")
  void health_shouldEmitHealthUp() {
    StepVerifier.create(healthIndicator.health())
        .expectNextMatches(h -> Status.UP.equals(h.getStatus()))
        .verifyComplete();
  }

  @Test
  @DisplayName("health() should complete without error")
  void health_shouldCompleteWithoutError() {
    StepVerifier.create(healthIndicator.health()).expectNextCount(1).verifyComplete();
  }

  // -------------------------------------------------------------------------
  // getGatewayStatus()
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getGatewayStatus() should return UP when health status is UP")
  void getGatewayStatus_shouldReturnUp_whenHealthIsUp() {
    StepVerifier.create(healthIndicator.getGatewayStatus())
        .expectNext(ServiceStatus.UP)
        .verifyComplete();
  }

  @Test
  @DisplayName("getGatewayStatus() should return DOWN when health status is DOWN")
  void getGatewayStatus_shouldReturnDown_whenHealthIsDown() {
    GatewayHealthIndicator spy = spy(healthIndicator);
    doReturn(Mono.just(Health.down().build())).when(spy).health();

    StepVerifier.create(spy.getGatewayStatus()).expectNext(ServiceStatus.DOWN).verifyComplete();
  }

  @Test
  @DisplayName("getGatewayStatus() should return DOWN when health status is UNKNOWN")
  void getGatewayStatus_shouldReturnDown_whenHealthIsUnknown() {
    GatewayHealthIndicator spy = spy(healthIndicator);
    doReturn(Mono.just(Health.unknown().build())).when(spy).health();

    StepVerifier.create(spy.getGatewayStatus()).expectNext(ServiceStatus.DOWN).verifyComplete();
  }

  @Test
  @DisplayName("getGatewayStatus() should return DOWN when health status is OUT_OF_SERVICE")
  void getGatewayStatus_shouldReturnDown_whenHealthIsOutOfService() {
    GatewayHealthIndicator spy = spy(healthIndicator);
    doReturn(Mono.just(Health.outOfService().build())).when(spy).health();

    StepVerifier.create(spy.getGatewayStatus()).expectNext(ServiceStatus.DOWN).verifyComplete();
  }

  @Test
  @DisplayName("getGatewayStatus() should return DOWN when health() emits an error")
  void getGatewayStatus_shouldReturnDown_whenHealthThrowsError() {
    GatewayHealthIndicator spy = spy(healthIndicator);
    doReturn(Mono.error(new RuntimeException("Health check failed"))).when(spy).health();

    StepVerifier.create(spy.getGatewayStatus()).expectNext(ServiceStatus.DOWN).verifyComplete();
  }

  @Test
  @DisplayName("getGatewayStatus() should delegate to health()")
  void getGatewayStatus_shouldCallHealthOnce() {
    GatewayHealthIndicator spy = spy(healthIndicator);

    StepVerifier.create(spy.getGatewayStatus()).expectNextCount(1).verifyComplete();

    verify(spy, times(1)).health();
  }

  @Test
  @DisplayName("getGatewayStatus() status code comparison should be case-insensitive")
  void getGatewayStatus_shouldBeCaseInsensitive_whenStatusCodeIsLowercase() {
    GatewayHealthIndicator spy = spy(healthIndicator);
    Health healthWithLowercaseUp = Health.status("up").build();
    doReturn(Mono.just(healthWithLowercaseUp)).when(spy).health();

    StepVerifier.create(spy.getGatewayStatus()).expectNext(ServiceStatus.UP).verifyComplete();
  }
}
