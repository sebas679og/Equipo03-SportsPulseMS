package com.sportspulse.teams.integration.msauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.sportspulse.teams.config.constants.ApiPaths;
import com.sportspulse.teams.exceptions.CustomBadGatewayException;
import com.sportspulse.teams.exceptions.CustomServiceUnavailableException;
import com.sportspulse.teams.exceptions.CustomUnauthorizedException;
import com.sportspulse.teams.integration.msauth.dto.UserResponse;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AuthClientImplTest {

  @Mock private WebClient authWebClient;

  @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

  @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  @InjectMocks private AuthClientImpl authClient;

  private static final String VALID_TOKEN = "valid-jwt-token";

  // -------------------------------------------------------------------------
  // Test helpers
  // -------------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  private void givenWebClientReturns(UserResponse responseBody) {
    doReturn(requestHeadersUriSpec).when(authWebClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
    doReturn(requestHeadersSpec).when(requestHeadersSpec).header(anyString(), anyString());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
    given(responseSpec.bodyToMono(UserResponse.class)).willReturn(Mono.justOrEmpty(responseBody));
  }

  @SuppressWarnings("unchecked")
  private void givenWebClientStatusTriggers(int statusCode) {
    doReturn(requestHeadersUriSpec).when(authWebClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
    doReturn(requestHeadersSpec).when(requestHeadersSpec).header(anyString(), anyString());
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();

    ClientResponse clientResponse = mock(ClientResponse.class);
    given(clientResponse.bodyToMono(String.class)).willReturn(Mono.just("Error body"));
    lenient().when(clientResponse.statusCode()).thenReturn(HttpStatusCode.valueOf(statusCode));

    given(responseSpec.onStatus(any(), any()))
        .willAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              Function<ClientResponse, Mono<? extends Throwable>> handler =
                  invocation.getArgument(1);
              if (predicate.test(HttpStatusCode.valueOf(statusCode))) {
                Mono<? extends Throwable> errorMono = handler.apply(clientResponse);
                given(responseSpec.bodyToMono(UserResponse.class))
                    .willReturn(errorMono.cast(UserResponse.class));
              }
              return responseSpec;
            });
  }

  // -------------------------------------------------------------------------
  // Successful response
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("isTokenValid() returns UserResponse when auth service responds successfully")
  void isTokenValid_whenServiceReturnsBody_returnsUserResponse() {
    UserResponse expected = mock(UserResponse.class);
    givenWebClientReturns(expected);

    UserResponse result = authClient.isTokenValid(VALID_TOKEN);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("isTokenValid() calls retrieve() on the built request")
  void isTokenValid_callsRetrieve() {
    givenWebClientReturns(mock(UserResponse.class));

    authClient.isTokenValid(VALID_TOKEN);

    then(requestHeadersSpec).should().retrieve();
  }

  @Test
  @DisplayName("isTokenValid() sets the Authorization header with the token")
  void isTokenValid_setsAuthorizationHeader() {
    givenWebClientReturns(mock(UserResponse.class));

    authClient.isTokenValid(VALID_TOKEN);

    then(requestHeadersSpec).should().header(eq(HttpHeaders.AUTHORIZATION), contains(VALID_TOKEN));
  }

  @Test
  @DisplayName("isTokenValid() targets the validate-token URI")
  void isTokenValid_targetsValidateTokenUri() {
    givenWebClientReturns(mock(UserResponse.class));

    authClient.isTokenValid(VALID_TOKEN);

    then(requestHeadersUriSpec).should().uri(ApiPaths.AuthService.VALIDATE_TOKEN);
  }

  // -------------------------------------------------------------------------
  // 401 Unauthorized → CustomUnauthorizedException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("isTokenValid() throws CustomUnauthorizedException on 401 response")
  void isTokenValid_when401_throwsCustomUnauthorizedException() {
    givenWebClientStatusTriggers(401);

    assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
        .isInstanceOf(CustomUnauthorizedException.class)
        .hasMessageContaining("Invalid or expired token");
  }

  // -------------------------------------------------------------------------
  // 403 Forbidden → CustomBadGatewayException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("isTokenValid() throws CustomBadGatewayException on 403 response")
  void isTokenValid_when403_throwsCustomBadGatewayException() {
    givenWebClientStatusTriggers(403);

    assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
        .isInstanceOf(CustomBadGatewayException.class)
        .hasMessageContaining("rejected the request");
  }

  // -------------------------------------------------------------------------
  // 5xx Server Error → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("isTokenValid() throws CustomServiceUnavailableException on 500 response")
  void isTokenValid_when500_throwsCustomServiceUnavailableException() {
    givenWebClientStatusTriggers(500);

    assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not available at this time");
  }

  @Test
  @DisplayName("isTokenValid() throws CustomServiceUnavailableException on 503 response")
  void isTokenValid_when503_throwsCustomServiceUnavailableException() {
    givenWebClientStatusTriggers(503);

    assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not available at this time");
  }

  // -------------------------------------------------------------------------
  // Empty body → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("isTokenValid() throws CustomServiceUnavailableException when body is empty")
  void isTokenValid_whenBodyIsEmpty_throwsCustomServiceUnavailableException() {
    givenWebClientReturns(null);

    assertThatThrownBy(() -> authClient.isTokenValid(VALID_TOKEN))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not available at this time");
  }
}
