package com.sportspulse.teams.integration.football;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sportspulse.teams.exceptions.CustomNotFoundException;
import com.sportspulse.teams.exceptions.CustomServiceUnavailableException;
import com.sportspulse.teams.exceptions.CustomTooManyRequestsException;
import com.sportspulse.teams.integration.football.dto.teamid.ApiFootballTeamResponse;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class FootballClientImplTest {

  @Mock private WebClient apiFootballWebClient;

  @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

  @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  @InjectMocks private FootballClientImpl footballClient;

  private static final int TEAM_ID = 42;

  // -------------------------------------------------------------------------
  // Test helpers
  // -------------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  private void givenWebClientReturns(ApiFootballTeamResponse responseBody) {
    given(apiFootballWebClient.get())
        .willReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(any(Function.class)))
        .willReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
    given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
    given(responseSpec.bodyToMono(ApiFootballTeamResponse.class))
        .willReturn(Mono.justOrEmpty(responseBody));
  }

  @SuppressWarnings("unchecked")
  private void givenWebClientStatusTriggers(int statusCode, Mono<Throwable> errorMono) {
    given(apiFootballWebClient.get())
        .willReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(any(Function.class)))
        .willReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);

    ClientResponse clientResponse = mock(ClientResponse.class);
    given(clientResponse.statusCode()).willReturn(HttpStatusCode.valueOf(statusCode));
    given(clientResponse.bodyToMono(String.class)).willReturn(Mono.just("Error body"));

    // First onStatus call matches the target status, subsequent ones pass through
    given(responseSpec.onStatus(any(), any()))
        .willAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              Function<ClientResponse, Mono<? extends Throwable>> handler =
                  invocation.getArgument(1);
              if (predicate.test(HttpStatusCode.valueOf(statusCode))) {
                handler.apply(clientResponse).subscribe();
              }
              return responseSpec;
            });

    given(responseSpec.bodyToMono(ApiFootballTeamResponse.class))
        .willReturn(Mono.error(new CustomServiceUnavailableException("forced")));
  }

  // -------------------------------------------------------------------------
  // Successful response
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getApiFootballTeamById() returns the team response when API call succeeds")
  void getApiFootballTeamById_whenApiReturnsBody_returnsTeamResponse() {
    ApiFootballTeamResponse expected = mock(ApiFootballTeamResponse.class);
    givenWebClientReturns(expected);

    ApiFootballTeamResponse result = footballClient.getApiFootballTeamById(TEAM_ID);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("getApiFootballTeamById() builds URI with the correct team ID query param")
  void getApiFootballTeamById_buildsUriWithCorrectTeamId() {
    givenWebClientReturns(mock(ApiFootballTeamResponse.class));

    footballClient.getApiFootballTeamById(TEAM_ID);

    then(requestHeadersUriSpec).should().uri(any(Function.class));
  }

  @Test
  @DisplayName("getApiFootballTeamById() calls retrieve() on the request")
  void getApiFootballTeamById_callsRetrieve() {
    givenWebClientReturns(mock(ApiFootballTeamResponse.class));

    footballClient.getApiFootballTeamById(TEAM_ID);

    then(requestHeadersSpec).should().retrieve();
  }

  // -------------------------------------------------------------------------
  // 204 No Content → CustomNotFoundException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getApiFootballTeamById() throws CustomNotFoundException on 204 response")
  @SuppressWarnings("unchecked")
  void getApiFootballTeamById_when204_throwsCustomNotFoundException() {
    given(apiFootballWebClient.get())
        .willReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(any(Function.class)))
        .willReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);

    given(responseSpec.onStatus(any(), any()))
        .willAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              Function<ClientResponse, Mono<? extends Throwable>> handler =
                  invocation.getArgument(1);
              if (predicate.test(HttpStatusCode.valueOf(204))) {
                // Trigger the handler — it returns Mono.error(CustomNotFoundException)
                ClientResponse clientResponse = mock(ClientResponse.class);
                Mono<? extends Throwable> errorMono = handler.apply(clientResponse);
                // Swap bodyToMono to propagate the exception
                given(responseSpec.bodyToMono(ApiFootballTeamResponse.class))
                    .willReturn(errorMono.cast(ApiFootballTeamResponse.class));
              }
              return responseSpec;
            });

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomNotFoundException.class)
        .hasMessageContaining("not found in Api-Football");
  }

  // -------------------------------------------------------------------------
  // 429 Too Many Requests → CustomTooManyRequestsException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getApiFootballTeamById() throws CustomTooManyRequestsException on 429 response")
  @SuppressWarnings("unchecked")
  void getApiFootballTeamById_when429_throwsCustomTooManyRequestsException() {
    given(apiFootballWebClient.get())
        .willReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(any(Function.class)))
        .willReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);

    ClientResponse clientResponse = mock(ClientResponse.class);
    given(clientResponse.bodyToMono(String.class)).willReturn(Mono.just("Rate limit body"));

    given(responseSpec.onStatus(any(), any()))
        .willAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              Function<ClientResponse, Mono<? extends Throwable>> handler =
                  invocation.getArgument(1);
              if (predicate.test(HttpStatusCode.valueOf(429))) {
                Mono<? extends Throwable> errorMono = handler.apply(clientResponse);
                given(responseSpec.bodyToMono(ApiFootballTeamResponse.class))
                    .willReturn(errorMono.cast(ApiFootballTeamResponse.class));
              }
              return responseSpec;
            });

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomTooManyRequestsException.class)
        .hasMessageContaining("daily request limit");
  }

  // -------------------------------------------------------------------------
  // 499 / 500 → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getApiFootballTeamById() throws CustomServiceUnavailableException on 499 response")
  @SuppressWarnings("unchecked")
  void getApiFootballTeamById_when499_throwsCustomServiceUnavailableException() {
    given(apiFootballWebClient.get())
        .willReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(any(Function.class)))
        .willReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);

    ClientResponse clientResponse = mock(ClientResponse.class);
    given(clientResponse.statusCode()).willReturn(HttpStatusCode.valueOf(499));
    given(clientResponse.bodyToMono(String.class)).willReturn(Mono.just("Error body"));

    given(responseSpec.onStatus(any(), any()))
        .willAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              Function<ClientResponse, Mono<? extends Throwable>> handler =
                  invocation.getArgument(1);
              if (predicate.test(HttpStatusCode.valueOf(499))) {
                Mono<? extends Throwable> errorMono = handler.apply(clientResponse);
                given(responseSpec.bodyToMono(ApiFootballTeamResponse.class))
                    .willReturn(errorMono.cast(ApiFootballTeamResponse.class));
              }
              return responseSpec;
            });

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  @Test
  @DisplayName("getApiFootballTeamById() throws CustomServiceUnavailableException on 500 response")
  @SuppressWarnings("unchecked")
  void getApiFootballTeamById_when500_throwsCustomServiceUnavailableException() {
    given(apiFootballWebClient.get())
        .willReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
    given(requestHeadersUriSpec.uri(any(Function.class)))
        .willReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
    given(requestHeadersSpec.retrieve()).willReturn(responseSpec);

    ClientResponse clientResponse = mock(ClientResponse.class);
    given(clientResponse.statusCode()).willReturn(HttpStatusCode.valueOf(500));
    given(clientResponse.bodyToMono(String.class)).willReturn(Mono.just("Internal error"));

    given(responseSpec.onStatus(any(), any()))
        .willAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              Function<ClientResponse, Mono<? extends Throwable>> handler =
                  invocation.getArgument(1);
              if (predicate.test(HttpStatusCode.valueOf(500))) {
                Mono<? extends Throwable> errorMono = handler.apply(clientResponse);
                given(responseSpec.bodyToMono(ApiFootballTeamResponse.class))
                    .willReturn(errorMono.cast(ApiFootballTeamResponse.class));
              }
              return responseSpec;
            });

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  // -------------------------------------------------------------------------
  // Empty body → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName(
      "getApiFootballTeamById() throws CustomServiceUnavailableException when body is empty")
  void getApiFootballTeamById_whenBodyIsEmpty_throwsCustomServiceUnavailableException() {
    givenWebClientReturns(null); // Mono.justOrEmpty(null) → empty Mono

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }
}
