package com.sportspulse.gateway.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.gateway.AbstractIntegrationTest;
import com.sportspulse.gateway.config.constants.ApiPaths;
import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.dto.responses.HealthResponse;
import com.sportspulse.gateway.utils.enums.ServiceStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

@DisplayName("GET /health - Integration Tests")
class HealthControllerIntegrationTest extends AbstractIntegrationTest {

  // Actuator response when the service is UP
  private static final String BODY_UP =
      """
      {"status":"UP"}
      """;

  // Actuator response when the service is DOWN
  private static final String BODY_DOWN =
      """
      {"status":"DOWN"}
      """;

  @Autowired private WebTestClient webTestClient;

  @AfterEach
  void resetWireMock() {
    wireMock.resetAll();
  }

  // -------------------------------------------------------------------------
  // Helpers to register stubs in WireMock
  // -------------------------------------------------------------------------

  /** Stub that responds 200 with status UP for the /actuator/health path. */
  private void stubServiceUp() {
    wireMock.stubFor(
        get(urlEqualTo(ApiPathsServices.Health.ACTUATOR_HEALTH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(BODY_UP)));
  }

  /** Stub that responds 200 with status DOWN for the /actuator/health path. */
  private void stubServiceDown() {
    wireMock.stubFor(
        get(urlEqualTo(ApiPathsServices.Health.ACTUATOR_HEALTH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(BODY_DOWN)));
  }

  /**
   * Stub that forces a timeout (delay greater than the one configured in WebClient, which is 2 s).
   * The value used is 3 000 ms to exceed the 2 s timeout defined in ServiceClientImpl.
   */
  private void stubServiceTimeout() {
    wireMock.stubFor(
        get(urlEqualTo(ApiPathsServices.Health.ACTUATOR_HEALTH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(BODY_UP)
                    .withFixedDelay(3_000)));
  }

  /** Stub that responds 200 with an empty body. */
  private void stubServiceEmptyBody() {
    wireMock.stubFor(
        get(urlEqualTo(ApiPathsServices.Health.ACTUATOR_HEALTH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("")));
  }

  /** Stub that responds 500 (server error). */
  private void stubServiceServerError() {
    wireMock.stubFor(
        get(urlEqualTo(ApiPathsServices.Health.ACTUATOR_HEALTH))
            .willReturn(aResponse().withStatus(500)));
  }

  // -------------------------------------------------------------------------
  // Test scenarios
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Scenario 1: all services UP")
  class AllServicesUp {

    @Test
    @DisplayName("Should return gateway=UP, dependencies=UP and all 7 services UP")
    void shouldReturnAllUp() {
      stubServiceUp();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(HealthResponse.class)
          .value(
              response -> {
                assertThat(response.getGateway()).isEqualTo(ServiceStatus.UP);
                assertThat(response.getDependencies()).isEqualTo(ServiceStatus.UP);
                assertThat(response.getServices()).hasSize(7);
                assertThat(response.getServices())
                    .allSatisfy(
                        (name, status) ->
                            assertThat(status)
                                .as("Service '%s' should be UP", name)
                                .isEqualTo(ServiceStatus.UP));
                assertThat(response.getTimestamp()).isNotNull();
              });
    }
  }

  @Nested
  @DisplayName("Scenario 2: a single service responds DOWN")
  class OneDependencyDown {

    /**
     * WireMock responds DOWN for /actuator/health for ALL services because they all point to the
     * same mock port. To test a single service DOWN in isolation, an additional WireMockServer on a
     * different port would be needed.
     *
     * <p>This test validates the business rule: if at least one service is DOWN, dependencies must
     * be DOWN — even though all 7 end up DOWN in this setup.
     *
     * <p>For a more surgical test, a second WireMockServer on a dedicated port assigned only to the
     * target service is recommended.
     */
    @Test
    @DisplayName("Should return dependencies=DOWN when at least one service is DOWN")
    void shouldReturnDependenciesDownWhenOneServiceIsDown() {
      // All microservices share the same WireMock by configuration,
      // so stubbing DOWN affects all 7.
      stubServiceDown();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(HealthResponse.class)
          .value(
              response -> {
                assertThat(response.getGateway()).isEqualTo(ServiceStatus.UP);
                assertThat(response.getDependencies()).isEqualTo(ServiceStatus.DOWN);
              });
    }
  }

  @Nested
  @DisplayName("Scenario 3: all services respond DOWN")
  class AllServicesDown {

    @Test
    @DisplayName("Should return gateway=UP, dependencies=DOWN and all 7 services DOWN")
    void shouldReturnAllDependenciesDown() {
      stubServiceDown();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(HealthResponse.class)
          .value(
              response -> {
                assertThat(response.getGateway()).isEqualTo(ServiceStatus.UP);
                assertThat(response.getDependencies()).isEqualTo(ServiceStatus.DOWN);
                assertThat(response.getServices()).hasSize(7);
                assertThat(response.getServices())
                    .allSatisfy(
                        (name, status) ->
                            assertThat(status)
                                .as("Service '%s' should be DOWN", name)
                                .isEqualTo(ServiceStatus.DOWN));
              });
    }
  }

  @Nested
  @DisplayName("Scenario 4: services do not respond (timeout)")
  class ServicesTimeout {

    @Test
    @DisplayName("Should return dependencies=DOWN when services take longer than 2 s")
    void shouldReturnDownOnTimeout() {
      stubServiceTimeout();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          // WebClient timeout is 2 s; we add margin so the response can arrive
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(HealthResponse.class)
          .value(
              response -> {
                assertThat(response.getGateway()).isEqualTo(ServiceStatus.UP);
                assertThat(response.getDependencies()).isEqualTo(ServiceStatus.DOWN);
                assertThat(response.getServices())
                    .allSatisfy(
                        (name, status) ->
                            assertThat(status)
                                .as("Service '%s' should be DOWN after timeout", name)
                                .isEqualTo(ServiceStatus.DOWN));
              });
    }
  }

  @Nested
  @DisplayName("Scenario 5: services respond with an empty body")
  class ServicesEmptyBody {

    @Test
    @DisplayName("Should return dependencies=DOWN when the response body is empty")
    void shouldReturnDownOnEmptyBody() {
      stubServiceEmptyBody();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(HealthResponse.class)
          .value(
              response -> {
                assertThat(response.getGateway()).isEqualTo(ServiceStatus.UP);
                assertThat(response.getDependencies()).isEqualTo(ServiceStatus.DOWN);
              });
    }
  }

  @Nested
  @DisplayName("Scenario 6: services respond with 500")
  class ServicesServerError {

    @Test
    @DisplayName("Should return dependencies=DOWN when services respond with 5xx")
    void shouldReturnDownOnServerError() {
      stubServiceServerError();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(HealthResponse.class)
          .value(
              response -> {
                assertThat(response.getGateway()).isEqualTo(ServiceStatus.UP);
                assertThat(response.getDependencies()).isEqualTo(ServiceStatus.DOWN);
                assertThat(response.getServices())
                    .allSatisfy(
                        (name, status) ->
                            assertThat(status)
                                .as("Service '%s' should be DOWN after 500", name)
                                .isEqualTo(ServiceStatus.DOWN));
              });
    }
  }

  @Nested
  @DisplayName("Scenario 7: response structure validation")
  class ResponseStructure {

    @Test
    @DisplayName("Should contain exactly the 7 expected microservices in the services map")
    void shouldContainExactlySevenServices() {
      stubServiceUp();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(HealthResponse.class)
          .value(
              response ->
                  assertThat(response.getServices())
                      .containsOnlyKeys(
                          "ms-auth",
                          "ms-leagues",
                          "ms-teams",
                          "ms-fixtures",
                          "ms-standings",
                          "ms-notifications",
                          "ms-dashboard"));
    }

    @Test
    @DisplayName("Should include a timestamp in the response")
    void shouldIncludeTimestamp() {
      stubServiceUp();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody(HealthResponse.class)
          .value(response -> assertThat(response.getTimestamp()).isNotNull());
    }

    @Test
    @DisplayName("Should respond with Content-Type application/json")
    void shouldRespondWithJsonContentType() {
      stubServiceUp();

      webTestClient
          .get()
          .uri(ApiPaths.HEALTH)
          .exchange()
          .expectStatus()
          .isOk()
          .expectHeader()
          .contentTypeCompatibleWith(org.springframework.http.MediaType.APPLICATION_JSON);
    }
  }
}
