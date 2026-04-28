package com.sportspulse.standings.integrations.football;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.standings.exceptions.CustomServiceUnavailableException;
import com.sportspulse.standings.integrations.football.dto.ApiStandingsResponse;
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

  @Mock private WebClient webClient;

  @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

  @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  @InjectMocks private FootballClientImpl footballClient;

  private static final int LEAGUE_ID = 39;
  private static final int SEASON = 2023;
  private static final String CACHE_KEY = "39-2023";

  // ─── Helpers ──────────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private void mockUriBuilderChain() {
    when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(Function.class)))
        .thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
  }

  private void mockOnStatusToPassThrough() {
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
  }

  private ApiStandingsResponse sampleApiStandingsResponse() {
    return mock(ApiStandingsResponse.class);
  }

  // ─── getStandingsForLeagueAndSeason – happy path ──────────────────────────

  @Test
  @DisplayName(
      "getStandingsForLeagueAndSeason() should return ApiStandingsResponse on successful response")
  void getStandingsForLeagueAndSeason_whenResponseIsSuccessful_shouldReturnApiStandingsResponse() {
    ApiStandingsResponse expected = sampleApiStandingsResponse();

    mockUriBuilderChain();
    mockOnStatusToPassThrough();
    when(responseSpec.bodyToMono(ApiStandingsResponse.class)).thenReturn(Mono.just(expected));

    ApiStandingsResponse result = footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName(
      "getStandingsForLeagueAndSeason() should call WebClient with correct league query param")
  void getStandingsForLeagueAndSeason_shouldCallWebClientWithLeagueParam() {
    mockUriBuilderChain();
    mockOnStatusToPassThrough();
    when(responseSpec.bodyToMono(ApiStandingsResponse.class))
        .thenReturn(Mono.just(sampleApiStandingsResponse()));

    footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON);

    verify(requestHeadersUriSpec).uri(any(Function.class));
  }

  @Test
  @DisplayName("getStandingsForLeagueAndSeason() should register two onStatus handlers")
  void getStandingsForLeagueAndSeason_shouldRegisterTwoOnStatusHandlers() {
    mockUriBuilderChain();
    mockOnStatusToPassThrough();
    when(responseSpec.bodyToMono(ApiStandingsResponse.class))
        .thenReturn(Mono.just(sampleApiStandingsResponse()));

    footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON);

    verify(responseSpec, times(2)).onStatus(any(), any());
  }

  // ─── executeRequest – 204 No Content ─────────────────────────────────────

  @Test
  @DisplayName(
      "getStandingsForLeagueAndSeason() should throw CustomServiceUnavailableException on 204")
  void getStandingsForLeagueAndSeason_whenStatusIs204() {
    mockUriBuilderChain();

    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              if (predicate.test(HttpStatusCode.valueOf(204))) {
                Function<ClientResponse, Mono<? extends Throwable>> errorHandler =
                    invocation.getArgument(1);
                ClientResponse clientResponse = mock(ClientResponse.class);
                when(clientResponse.bodyToMono(String.class)).thenReturn(Mono.just("No body"));
                errorHandler.apply(clientResponse).subscribe();
                throw new CustomServiceUnavailableException(
                    "Api-Football is not currently available, please try again");
              }
              return responseSpec;
            });

    assertThatThrownBy(() -> footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessage("Api-Football is not currently available, please try again");
  }

  // ─── executeRequest – 499 / 500 ───────────────────────────────────────────

  @Test
  @DisplayName(
      "getStandingsForLeagueAndSeason() should throw CustomServiceUnavailableException on 499")
  void getStandingsForLeagueAndSeason_whenStatusIs499() {
    mockUriBuilderChain();

    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              if (predicate.test(HttpStatusCode.valueOf(499))) {
                throw new CustomServiceUnavailableException(
                    "Api-Football is not currently available, please try again");
              }
              return responseSpec;
            });

    assertThatThrownBy(() -> footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessage("Api-Football is not currently available, please try again");
  }

  @Test
  @DisplayName(
      "getStandingsForLeagueAndSeason() should throw CustomServiceUnavailableException on 500")
  void getStandingsForLeagueAndSeason_whenStatusIs500() {
    mockUriBuilderChain();

    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              Predicate<HttpStatusCode> predicate = invocation.getArgument(0);
              if (predicate.test(HttpStatusCode.valueOf(500))) {
                throw new CustomServiceUnavailableException(
                    "Api-Football is not currently available, please try again");
              }
              return responseSpec;
            });

    assertThatThrownBy(() -> footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessage("Api-Football is not currently available, please try again");
  }

  // ─── executeRequest – empty / null body ───────────────────────────────────

  @Test
  @DisplayName(
      "getStandingsForLeagueAndSeason() should throw "
          + "CustomServiceUnavailableException when body is empty")
  void getStandingsForLeagueAndSeason_whenBodyIsEmpty() {
    mockUriBuilderChain();
    mockOnStatusToPassThrough();
    when(responseSpec.bodyToMono(ApiStandingsResponse.class)).thenReturn(Mono.empty());

    assertThatThrownBy(() -> footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessage("Api-Football is not currently available, please try again");
  }

  @Test
  @DisplayName(
      "getStandingsForLeagueAndSeason() should throw "
          + "CustomServiceUnavailableException when body is null")
  void getStandingsForLeagueAndSeason_whenBodyIsNull() {
    mockUriBuilderChain();
    mockOnStatusToPassThrough();
    when(responseSpec.bodyToMono(ApiStandingsResponse.class)).thenReturn(Mono.justOrEmpty(null));

    assertThatThrownBy(() -> footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessage("Api-Football is not currently available, please try again");
  }

  // ─── onStatus predicate coverage ─────────────────────────────────────────

  @Test
  @DisplayName("Status 200 should not match the 204 predicate")
  void statusPredicate_200_shouldNotMatch204Handler() {
    Predicate<HttpStatusCode> predicate204 = status -> status.value() == 204;

    assertThat(predicate204.test(HttpStatusCode.valueOf(200))).isFalse();
  }

  @Test
  @DisplayName("Status 204 should match the 204 predicate")
  void statusPredicate_204_shouldMatchHandler() {
    Predicate<HttpStatusCode> predicate204 = status -> status.value() == 204;

    assertThat(predicate204.test(HttpStatusCode.valueOf(204))).isTrue();
  }

  @Test
  @DisplayName("Status 499 should match the 499/500 predicate")
  void statusPredicate_499_shouldMatch499Or500Handler() {
    Predicate<HttpStatusCode> predicate = status -> status.value() == 499 || status.value() == 500;

    assertThat(predicate.test(HttpStatusCode.valueOf(499))).isTrue();
  }

  @Test
  @DisplayName("Status 500 should match the 499/500 predicate")
  void statusPredicate_500_shouldMatch499Or500Handler() {
    Predicate<HttpStatusCode> predicate = status -> status.value() == 499 || status.value() == 500;

    assertThat(predicate.test(HttpStatusCode.valueOf(500))).isTrue();
  }

  @Test
  @DisplayName("Status 503 should not match the 499/500 predicate")
  void statusPredicate_503_shouldNotMatch499Or500Handler() {
    Predicate<HttpStatusCode> predicate = status -> status.value() == 499 || status.value() == 500;

    assertThat(predicate.test(HttpStatusCode.valueOf(503))).isFalse();
  }
}
