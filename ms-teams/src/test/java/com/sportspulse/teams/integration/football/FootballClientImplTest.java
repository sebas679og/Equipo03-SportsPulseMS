package com.sportspulse.teams.integration.football;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import com.sportspulse.teams.exceptions.CustomServiceUnavailableException;
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
  private static final int LEAGUE_ID = 10;
  private static final int SEASON = 2024;

  // -------------------------------------------------------------------------
  // Test helpers
  // -------------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  private void givenWebClientReturns(ApiFootballTeamResponse responseBody) {
    doReturn(requestHeadersUriSpec).when(apiFootballWebClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
    doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
    given(responseSpec.bodyToMono(ApiFootballTeamResponse.class))
        .willReturn(Mono.justOrEmpty(responseBody));
  }

  @SuppressWarnings("unchecked")
  private void givenWebClientStatusTriggers(int statusCode) {
    doReturn(requestHeadersUriSpec).when(apiFootballWebClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(Function.class));
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
                given(responseSpec.bodyToMono(ApiFootballTeamResponse.class))
                    .willReturn(errorMono.cast(ApiFootballTeamResponse.class));
              }
              return responseSpec;
            });
  }

  // -------------------------------------------------------------------------
  // getApiFootballTeamById() — successful response
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
  @DisplayName("getApiFootballTeamById() calls retrieve() on the request")
  void getApiFootballTeamById_callsRetrieve() {
    givenWebClientReturns(mock(ApiFootballTeamResponse.class));

    footballClient.getApiFootballTeamById(TEAM_ID);

    then(requestHeadersSpec).should().retrieve();
  }

  @Test
  @DisplayName("getApiFootballTeamById() builds URI with a Function for query params")
  void getApiFootballTeamById_buildsUriWithFunction() {
    givenWebClientReturns(mock(ApiFootballTeamResponse.class));

    footballClient.getApiFootballTeamById(TEAM_ID);

    then(requestHeadersUriSpec).should().uri(any(Function.class));
  }

  // -------------------------------------------------------------------------
  // getApiFootballTeamById() — 204 → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getApiFootballTeamById() throws CustomServiceUnavailableException on 204 response")
  void getApiFootballTeamById_when204_throwsCustomServiceUnavailableException() {
    givenWebClientStatusTriggers(204);

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  // -------------------------------------------------------------------------
  // getApiFootballTeamById() — 499/500 → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getApiFootballTeamById() throws CustomServiceUnavailableException on 499 response")
  void getApiFootballTeamById_when499_throwsCustomServiceUnavailableException() {
    givenWebClientStatusTriggers(499);

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  @Test
  @DisplayName("getApiFootballTeamById() throws CustomServiceUnavailableException on 500 response")
  void getApiFootballTeamById_when500_throwsCustomServiceUnavailableException() {
    givenWebClientStatusTriggers(500);

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  // -------------------------------------------------------------------------
  // getApiFootballTeamById() — empty body → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName(
      "getApiFootballTeamById() throws CustomServiceUnavailableException when body is empty")
  void getApiFootballTeamById_whenBodyIsEmpty_throwsCustomServiceUnavailableException() {
    givenWebClientReturns(null);

    assertThatThrownBy(() -> footballClient.getApiFootballTeamById(TEAM_ID))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  // -------------------------------------------------------------------------
  // getApiFootballTeamByLeagueAndSeason() — successful response
  // -------------------------------------------------------------------------

  @Test
  @DisplayName(
      "getApiFootballTeamByLeagueAndSeason() returns the team response when API call succeeds")
  void getApiFootballTeamByLeagueAndSeason_whenApiReturnsBody_returnsTeamResponse() {
    ApiFootballTeamResponse expected = mock(ApiFootballTeamResponse.class);
    givenWebClientReturns(expected);

    ApiFootballTeamResponse result =
        footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("getApiFootballTeamByLeagueAndSeason() calls retrieve() on the request")
  void getApiFootballTeamByLeagueAndSeason_callsRetrieve() {
    givenWebClientReturns(mock(ApiFootballTeamResponse.class));

    footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON);

    then(requestHeadersSpec).should().retrieve();
  }

  @Test
  @DisplayName("getApiFootballTeamByLeagueAndSeason() builds URI with a Function for query params")
  void getApiFootballTeamByLeagueAndSeason_buildsUriWithFunction() {
    givenWebClientReturns(mock(ApiFootballTeamResponse.class));

    footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON);

    then(requestHeadersUriSpec).should().uri(any(Function.class));
  }

  // -------------------------------------------------------------------------
  // getApiFootballTeamByLeagueAndSeason() — 204 → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("throws CustomServiceUnavailableException on 204 response")
  void getApiFootballTeamByLeagueAndSeason_when204_throwsCustomServiceUnavailableException() {
    givenWebClientStatusTriggers(204);

    assertThatThrownBy(() -> footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  // -------------------------------------------------------------------------
  // getApiFootballTeamByLeagueAndSeason() — 499/500 → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("throws CustomServiceUnavailableException on 499 response")
  void getApiFootballTeamByLeagueAndSeason_when499_throwsCustomServiceUnavailableException() {
    givenWebClientStatusTriggers(499);

    assertThatThrownBy(() -> footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  @Test
  @DisplayName("throws CustomServiceUnavailableException on 500 response")
  void getApiFootballTeamByLeagueAndSeason_when500_throwsCustomServiceUnavailableException() {
    givenWebClientStatusTriggers(500);

    assertThatThrownBy(() -> footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }

  // -------------------------------------------------------------------------
  // getApiFootballTeamByLeagueAndSeason() — empty body → CustomServiceUnavailableException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("throws CustomServiceUnavailableException when body is empty")
  void whenBodyIsEmpty_throwsCustomServiceUnavailableException() {
    givenWebClientReturns(null);

    assertThatThrownBy(() -> footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("not currently available");
  }
}
