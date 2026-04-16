package com.sportspulse.gateway.integration.client;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.integration.dtos.responses.ServiceResponse;
import com.sportspulse.gateway.utils.enums.ServiceStatus;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceClientImpl Tests")
class ServiceClientImplTest {

  // -------------------------------------------------------------------------
  // Mocks — WebClient fluent chain
  // -------------------------------------------------------------------------

  @Mock private WebClient webClient;
  @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
  @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
  @Mock private WebClient.ResponseSpec responseSpec;

  private ServiceClientImpl serviceClient;

  private static final String SERVICE_NAME = "test-service";
  private static final String BASE_URL = "http://localhost:8081";

  @BeforeEach
  void setUp() {
    serviceClient = new ServiceClientImpl(webClient);

    // doReturn avoids the unchecked-cast issue caused by WebClient's wildcard generics
    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private void givenResponseBody(ServiceResponse response) {
    when(responseSpec.bodyToMono(ServiceResponse.class)).thenReturn(Mono.just(response));
  }

  private void givenResponseError(Throwable error) {
    when(responseSpec.bodyToMono(ServiceResponse.class)).thenReturn(Mono.error(error));
  }

  private ServiceResponse responseWithStatus(ServiceStatus status) {
    return ServiceResponse.builder().status(status).build();
  }

  // =========================================================================
  // getHealthService()
  // =========================================================================

  @Nested
  @DisplayName("getHealthService() - happy path")
  class HappyPath {

    @Test
    @DisplayName("should return UP when actuator responds with status UP")
    void shouldReturnUp_whenActuatorStatusIsUp() {
      givenResponseBody(responseWithStatus(ServiceStatus.UP));

      StepVerifier.create(serviceClient.getHealthService(SERVICE_NAME, BASE_URL))
          .expectNext(ServiceStatus.UP)
          .verifyComplete();
    }

    @Test
    @DisplayName("should call the correct URI (baseUrl + actuator path)")
    void shouldCallCorrectUri() {
      givenResponseBody(responseWithStatus(ServiceStatus.UP));

      serviceClient.getHealthService(SERVICE_NAME, BASE_URL).block();

      verify(requestHeadersUriSpec).uri(BASE_URL + ApiPathsServices.Health.ACTUATOR_HEALTH);
    }
  }

  // =========================================================================
  // mapToStatus() — DOWN scenarios
  // =========================================================================

  @Nested
  @DisplayName("getHealthService() - mapToStatus DOWN scenarios")
  class MapToStatusDown {

    @Test
    @DisplayName("should return DOWN when response body is null")
    void shouldReturnDown_whenResponseIsNull() {
      when(responseSpec.bodyToMono(ServiceResponse.class))
          .thenReturn(Mono.just(ServiceResponse.builder().build()));
      // ServiceResponse has null status by default

      StepVerifier.create(serviceClient.getHealthService(SERVICE_NAME, BASE_URL))
          .expectNext(ServiceStatus.DOWN)
          .verifyComplete();
    }

    @Test
    @DisplayName("should return DOWN when response status field is null")
    void shouldReturnDown_whenResponseStatusIsNull() {
      ServiceResponse response = ServiceResponse.builder().build();
      givenResponseBody(response);

      StepVerifier.create(serviceClient.getHealthService(SERVICE_NAME, BASE_URL))
          .expectNext(ServiceStatus.DOWN)
          .verifyComplete();
    }

    @Test
    @DisplayName("should return DOWN when actuator status is DOWN")
    void shouldReturnDown_whenActuatorStatusIsDown() {
      givenResponseBody(responseWithStatus(ServiceStatus.DOWN));

      StepVerifier.create(serviceClient.getHealthService(SERVICE_NAME, BASE_URL))
          .expectNext(ServiceStatus.DOWN)
          .verifyComplete();
    }
  }

  // =========================================================================
  // onErrorResume — error handling
  // =========================================================================

  @Nested
  @DisplayName("getHealthService() - onErrorResume scenarios")
  class ErrorHandling {

    @Test
    @DisplayName("should return DOWN when WebClient throws a runtime exception")
    void shouldReturnDown_whenWebClientThrowsRuntimeException() {
      givenResponseError(new RuntimeException("Connection refused"));

      StepVerifier.create(serviceClient.getHealthService(SERVICE_NAME, BASE_URL))
          .expectNext(ServiceStatus.DOWN)
          .verifyComplete();
    }

    @Test
    @DisplayName("should return DOWN when WebClient throws a WebClientResponseException (e.g. 503)")
    void shouldReturnDown_whenWebClientResponseExceptionThrown() {
      givenResponseError(
          WebClientResponseException.create(503, "Service Unavailable", null, null, null));

      StepVerifier.create(serviceClient.getHealthService(SERVICE_NAME, BASE_URL))
          .expectNext(ServiceStatus.DOWN)
          .verifyComplete();
    }

    @Test
    @DisplayName("should return DOWN when request times out")
    void shouldReturnDown_whenRequestTimesOut() {
      // Simulate a never-ending response that will be cut by the 2-second timeout
      when(responseSpec.bodyToMono(ServiceResponse.class)).thenReturn(Mono.never());

      StepVerifier.withVirtualTime(() -> serviceClient.getHealthService(SERVICE_NAME, BASE_URL))
          .thenAwait(Duration.ofSeconds(3))
          .expectNext(ServiceStatus.DOWN)
          .verifyComplete();
    }

    @Test
    @DisplayName("should not propagate errors — Mono must always complete normally")
    void shouldAlwaysComplete_neverPropagateError() {
      givenResponseError(new IllegalStateException("Unexpected error"));

      StepVerifier.create(serviceClient.getHealthService(SERVICE_NAME, BASE_URL))
          .expectNextCount(1)
          .verifyComplete(); // must NOT call verifyError()
    }
  }
}
