package com.sportspulse.gateway;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.config.constants.InternalHeaders;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

@Tag("redis")
class GatewayIntegrationTest extends AbstractIntegrationTest {

  @Autowired private WebTestClient webTestClient;

  @Autowired private ReactiveStringRedisTemplate redisTemplate;

  @Autowired
  private CircuitBreakerRegistry circuitBreakerRegistry;

  @BeforeEach
  void setUp() {
    redisTemplate.execute(connection -> connection.serverCommands().flushAll()).then().block();
    circuitBreakerRegistry.getAllCircuitBreakers()
            .forEach(CircuitBreaker::reset);
    wireMock.resetAll();
  }

  @Test
  void shouldProxyLoginRequestToAuthService() {
    wireMock.stubFor(
        WireMock.post(WireMock.urlEqualTo("/api/auth/login"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                        {
                          "token": "eyJhbGciOiJIUzI1NiJ9.test",
                          "type": "Bearer"
                        }
                        """)));

    webTestClient
        .post()
        .uri("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
                {
                  "username": "sebas",
                  "password": "secret"
                }
                """)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.token")
        .isEqualTo("eyJhbGciOiJIUzI1NiJ9.test")
        .jsonPath("$.type")
        .isEqualTo("Bearer");

    wireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/api/auth/login")));
  }

  @Test
  void shouldProxyLoginRequestWithFullResponseDetails() {
    wireMock.stubFor(
        WireMock.post(WireMock.urlEqualTo("/api/auth/login"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withHeader("X-Request-Id", "abc-123")
                    .withHeader("X-Auth-Service", "ms-auth")
                    .withBody(
                        """
                    {
                      "token": "eyJhbGciOiJIUzI1NiJ9.test",
                      "type": "Bearer",
                      "expiresIn": 3600
                    }
                    """)));

    EntityExchangeResult<byte[]> result =
        webTestClient
            .post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-Forwarded-For", "192.168.1.1")
            .bodyValue(
                """
            {
              "username": "sebas",
              "password": "secret"
            }
            """)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectHeader()
            .valueEquals("X-Request-Id", "abc-123")
            .expectHeader()
            .valueEquals("X-Auth-Service", "ms-auth")
            .expectBody()
            .jsonPath("$.token")
            .isEqualTo("eyJhbGciOiJIUzI1NiJ9.test")
            .jsonPath("$.type")
            .isEqualTo("Bearer")
            .jsonPath("$.expiresIn")
            .isEqualTo(3600)
            .returnResult();

    assertThat(result.getStatus()).isEqualTo(HttpStatus.OK);

    HttpHeaders headers = result.getResponseHeaders();
    assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    assertThat(headers.get("X-Request-Id")).containsExactly("abc-123");
    assertThat(headers.get("X-Auth-Service")).containsExactly("ms-auth");

    byte[] rawBody = result.getResponseBody();
    assertThat(rawBody).isNotNull();
    String bodyAsString = new String(rawBody, StandardCharsets.UTF_8);
    assertThat(bodyAsString).contains("token", "Bearer", "3600");

    wireMock.verify(
        WireMock.postRequestedFor(WireMock.urlEqualTo("/api/auth/login"))
            .withHeader("Content-Type", WireMock.containing("application/json"))
            .withRequestBody(WireMock.matchingJsonPath("$.username", WireMock.equalTo("sebas")))
            .withRequestBody(WireMock.matchingJsonPath("$.password", WireMock.equalTo("secret"))));
  }

  @Test
  void shouldReturn503WhenUpstreamServiceIsUnavailable() {
    wireMock.stubFor(
        WireMock.post(WireMock.urlEqualTo("/api/auth/login"))
            .willReturn(WireMock.aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

    EntityExchangeResult<byte[]> result =
        webTestClient
            .post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
            {
              "username": "sebas",
              "password": "secret"
            }
            """)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
            .expectBody()
            .returnResult();

    assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

    byte[] rawBody = result.getResponseBody();
    assertThat(rawBody).isNotNull();
    String bodyAsString = new String(rawBody, StandardCharsets.UTF_8);
    assertThat(bodyAsString).isNotBlank();

    wireMock.verify(WireMock.moreThanOrExactly(1),
            WireMock.postRequestedFor(WireMock.urlEqualTo("/api/auth/login")));
  }

  @Test
  void shouldReturn404WhenRouteDoesNotExist() {
    EntityExchangeResult<byte[]> result =
        webTestClient
            .get()
            .uri("/api/non/existing/route")
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .returnResult();

    assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    wireMock.verify(0, WireMock.anyRequestedFor(WireMock.anyUrl()));
  }

  @Test
  void shouldReturn404WhenAccessingProtectedValidateEndpoint() {
    EntityExchangeResult<byte[]> result =
        webTestClient
            .get()
            .uri(ApiPathsServices.Auth.VALIDATE_TOKEN)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND)
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.description")
            .isEqualTo("Not found")
            .returnResult();

    assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    wireMock.verify(0, WireMock.anyRequestedFor(WireMock.anyUrl()));
  }

  @Test
  void shouldReturn429AfterExceedingBruteForceRateLimitOnLogin() {
    wireMock.stubFor(
        WireMock.post(WireMock.urlEqualTo("/api/auth/login"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                                {
                                  "token": "eyJhbGciOiJIUzI1NiJ9.test",
                                  "type": "Bearer"
                                }
                                """)));

    for (int i = 1; i <= 5; i++) {
      webTestClient
          .post()
          .uri(ApiPathsServices.Auth.LOGIN)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(
              """
                    { "username": "sebas", "password": "secret" }
                    """)
          .exchange()
          .expectStatus()
          .isOk();
    }

    EntityExchangeResult<byte[]> result =
        webTestClient
            .post()
            .uri(ApiPathsServices.Auth.LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                """
                { "username": "sebas", "password": "secret" }
                """)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.description")
            .exists()
            .returnResult();

    assertThat(result.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

    wireMock.verify(
        WireMock.moreThanOrExactly(5),
        WireMock.postRequestedFor(WireMock.urlEqualTo("/api/auth/login")));
  }

  @Test
  void shouldReturn429AfterExceedingStandardRateLimitOnAuthEndpoints() {
    wireMock.stubFor(
        WireMock.get(WireMock.urlMatching("/api/auth/.*"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

    for (int i = 1; i <= 60; i++) {
      webTestClient
          .get()
          .uri(ApiPathsServices.Auth.BASE + "/profile")
          .exchange()
          .expectStatus()
          .isOk();
    }

    webTestClient
        .get()
        .uri(ApiPathsServices.Auth.BASE + "/profile")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

    wireMock.verify(60, WireMock.getRequestedFor(WireMock.urlEqualTo("/api/auth/profile")));
  }

  @Test
  void shouldTrackRateLimitCounterPerIp() {
    wireMock.stubFor(
        WireMock.post(WireMock.urlEqualTo("/api/auth/login"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                                {
                                  "token": "eyJhbGciOiJIUzI1NiJ9.test",
                                  "type": "Bearer"
                                }
                                """)));

    String ipA = "10.0.0.1";
    String ipB = "10.0.0.2";

    for (int i = 1; i <= 5; i++) {
      webTestClient
          .post()
          .uri(ApiPathsServices.Auth.LOGIN)
          .header(InternalHeaders.CLIENT_IP_HEADER, ipA)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(
              """
                    { "username": "sebas", "password": "secret" }
                    """)
          .exchange()
          .expectStatus()
          .isOk();
    }

    webTestClient
        .post()
        .uri(ApiPathsServices.Auth.LOGIN)
        .header(InternalHeaders.CLIENT_IP_HEADER, ipA)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
                { "username": "sebas", "password": "secret" }
                """)
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

    webTestClient
        .post()
        .uri(ApiPathsServices.Auth.LOGIN)
        .header(InternalHeaders.CLIENT_IP_HEADER, ipB)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
                { "username": "sebas", "password": "secret" }
                """)
        .exchange()
        .expectStatus()
        .isOk();

    wireMock.verify(
        WireMock.moreThanOrExactly(5),
        WireMock.postRequestedFor(WireMock.urlEqualTo("/api/auth/login"))
            .withHeader(InternalHeaders.CLIENT_IP_HEADER, WireMock.equalTo(ipA)));

    wireMock.verify(
        WireMock.exactly(1),
        WireMock.postRequestedFor(WireMock.urlEqualTo("/api/auth/login"))
            .withHeader(InternalHeaders.CLIENT_IP_HEADER, WireMock.equalTo(ipB)));
  }

  @Test
  void shouldStoreIndependentRedisKeysPerIp() {
    wireMock.stubFor(
        WireMock.post(WireMock.urlEqualTo("/api/auth/login"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

    String ipA = "192.168.1.10";
    String ipB = "192.168.1.20";

    webTestClient
        .post()
        .uri(ApiPathsServices.Auth.LOGIN)
        .header(InternalHeaders.CLIENT_IP_HEADER, ipA)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            { "username": "sebas", "password": "secret" }
            """)
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .post()
        .uri(ApiPathsServices.Auth.LOGIN)
        .header(InternalHeaders.CLIENT_IP_HEADER, ipB)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            { "username": "sebas", "password": "secret" }
            """)
        .exchange()
        .expectStatus()
        .isOk();

    webTestClient
        .post()
        .uri(ApiPathsServices.Auth.LOGIN)
        .header(InternalHeaders.CLIENT_IP_HEADER, ipB)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            """
            { "username": "sebas", "password": "secret" }
            """)
        .exchange()
        .expectStatus()
        .isOk();

    Set<String> keys = redisTemplate.keys("*").collect(Collectors.toSet()).block();
    assertThat(keys).isNotNull();

    boolean hasKeyForIpA = keys.stream().anyMatch(k -> k.contains(ipA));
    boolean hasKeyForIpB = keys.stream().anyMatch(k -> k.contains(ipB));
    assertThat(hasKeyForIpA).isTrue();
    assertThat(hasKeyForIpB).isTrue();

    String keyA = keys.stream().filter(k -> k.contains(ipA)).findFirst().orElseThrow();
    String keyB = keys.stream().filter(k -> k.contains(ipB)).findFirst().orElseThrow();

    assertThat(redisTemplate.opsForValue().get(keyA).block()).isEqualTo("1");
    assertThat(redisTemplate.opsForValue().get(keyB).block()).isEqualTo("2");
  }
}
