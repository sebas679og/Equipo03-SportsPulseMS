package com.sportspulse.fixtures.integartion.msauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.fixtures.config.constants.ApiPaths;
import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomServiceUnavailableException;
import com.sportspulse.fixtures.exceptions.CustomUnauthorizedException;
import com.sportspulse.fixtures.integration.msauth.AuthClientImpl;
import com.sportspulse.fixtures.integration.msauth.dto.UserResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AuthClientImpl Unit Tests")
class AuthClientImplTest {

  private MockWebServer mockWebServer;
  private AuthClientImpl authClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  // ─────────────────────────────────────────────
  // Lifecycle
  // ─────────────────────────────────────────────

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    WebClient webClient =
        WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    authClient = new AuthClientImpl(webClient);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  // ─────────────────────────────────────────────
  // Fixtures
  // ─────────────────────────────────────────────

  private static final String VALID_TOKEN = "valid-jwt-token";
  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  private UserResponse buildUserResponse() {
    return new UserResponse(true, USER_ID, "john.doe", "ROLE_USER");
  }

  private MockResponse jsonResponse(Object body) throws Exception {
    return new MockResponse()
        .setResponseCode(200)
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(objectMapper.writeValueAsString(body));
  }

  private MockResponse statusOnlyResponse(int statusCode) {
    return new MockResponse()
        .setResponseCode(statusCode)
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody("No body");
  }

  // ─────────────────────────────────────────────
  // isTokenValid() — happy path
  // ─────────────────────────────────────────────

  @Nested
  @DisplayName("Happy path")
  class HappyPath {

    @Test
    @DisplayName("Returns UserResponse when token is valid")
    void shouldReturnUserResponse_whenTokenIsValid() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildUserResponse()));

      UserResponse result = authClient.isTokenValid(VALID_TOKEN);

      assertThat(result).isNotNull();
      assertThat(result.valid()).isTrue();
      assertThat(result.userId()).isEqualTo(USER_ID);
      assertThat(result.username()).isEqualTo("john.doe");
      assertThat(result.role()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Sends Authorization header with Bearer prefix")
    void shouldSendAuthorizationHeader_withBearerPrefix() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildUserResponse()));

      authClient.isTokenValid(VALID_TOKEN);

      RecordedRequest request = mockWebServer.takeRequest();
      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
      assertThat(authHeader).isNotNull().startsWith("Bearer ").endsWith(VALID_TOKEN);
    }

    @Test
    @DisplayName("Sends request to the validate-token endpoint")
    void shouldSendRequestToValidateTokenEndpoint() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildUserResponse()));

      authClient.isTokenValid(VALID_TOKEN);

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getPath()).isEqualTo(ApiPaths.AuthService.VALIDATE_TOKEN);
    }

    @Test
    @DisplayName("Sends GET request")
    void shouldSendGetRequest() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildUserResponse()));

      authClient.isTokenValid(VALID_TOKEN);

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getMethod()).isEqualTo("GET");
    }
  }

  // ─────────────────────────────────────────────
  // isTokenValid() — error handling
  // ─────────────────────────────────────────────

  @Nested
  @DisplayName("Error handling")
  class ErrorHandling {

    @Test
    @DisplayName("Throws CustomUnauthorizedException on HTTP 401")
    void shouldThrowUnauthorized_on401() {
      mockWebServer.enqueue(statusOnlyResponse(401));

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomUnauthorizedException.class)
          .hasMessageContaining("Invalid or expired token");
    }

    @Test
    @DisplayName("Throws CustomUnauthorizedException on 401 with empty body")
    void shouldThrowUnauthorized_on401WithEmptyBody() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(401)
              .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomUnauthorizedException.class)
          .hasMessageContaining("Invalid or expired token");
    }

    @Test
    @DisplayName("Throws CustomBadGatewayException on HTTP 403")
    void shouldThrowBadGateway_on403() {
      mockWebServer.enqueue(statusOnlyResponse(403));

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomBadGatewayException.class)
          .hasMessageContaining("Session validation service rejected the request");
    }

    @Test
    @DisplayName("Throws CustomBadGatewayException on 403 with empty body")
    void shouldThrowBadGateway_on403WithEmptyBody() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(403)
              .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomBadGatewayException.class)
          .hasMessageContaining("Session validation service rejected the request");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 500")
    void shouldThrowServiceUnavailable_on500() {
      mockWebServer.enqueue(statusOnlyResponse(500));

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Session validation service is not available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 502")
    void shouldThrowServiceUnavailable_on502() {
      mockWebServer.enqueue(statusOnlyResponse(502));

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Session validation service is not available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 503")
    void shouldThrowServiceUnavailable_on503() {
      mockWebServer.enqueue(statusOnlyResponse(503));

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Session validation service is not available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException when API returns null body")
    void shouldThrowServiceUnavailable_whenBodyIsNull() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody("null"));

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Session validation service is not available");
    }

    @Test
    @DisplayName("Throws CustomBadGatewayException when server is unreachable")
    void shouldThrowBadGateway_whenServerIsUnreachable() throws Exception {
      // Shut down the server to simulate a network-level failure (connection refused),
      // which triggers WebClientRequestException -> onErrorMap -> CustomBadGatewayException.
      mockWebServer.shutdown();

      assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
          .isInstanceOf(CustomBadGatewayException.class)
          .hasMessageContaining("Session validation service is unreachable");
    }
  }
}
