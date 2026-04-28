package com.sportspulse.standings.service.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.standings.exceptions.CustomBadGatewayException;
import com.sportspulse.standings.exceptions.CustomNotFoundException;
import com.sportspulse.standings.exceptions.CustomTooManyRequestsException;
import com.sportspulse.standings.integrations.football.FootballClient;
import com.sportspulse.standings.integrations.football.dto.ApiResponse;
import com.sportspulse.standings.integrations.football.dto.ApiStandingsResponse;
import com.sportspulse.standings.services.complements.ApiFootballStandingsFetcher;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiFootballStandingsFetcher")
class ApiFootballStandingsFetcherTest {

  private static final int LEAGUE_ID = 39;
  private static final int SEASON = 2023;

  private static final ApiResponse ANY_RESPONSE = new ApiResponse(null);

  @Mock private FootballClient footballClient;

  @InjectMocks private ApiFootballStandingsFetcher fetcher;

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Builds a minimal valid response with no errors and the given response list. The scalar fields
   * (get, results) are set to neutral defaults; paging/parameters are null.
   */
  private ApiStandingsResponse responseWith(List<ApiResponse> responseBody) {
    return new ApiStandingsResponse("standings", null, List.of(), 0, null, responseBody);
  }

  /** Builds a response that carries the supplied error entries and a null response body. */
  private ApiStandingsResponse responseWithErrors(List<Object> errors) {
    return new ApiStandingsResponse("standings", null, errors, 0, null, null);
  }

  // ---------------------------------------------------------------------------
  // Happy-path
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("fetchValidatedStandings – success scenarios")
  class SuccessScenarios {

    @Test
    @DisplayName("returns the API response when standings are present")
    void returnsResponse_whenStandingsArePresent() {
      var apiResponse = responseWith(List.of(ANY_RESPONSE));
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      ApiStandingsResponse result = fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON);

      assertThat(result).isEqualTo(apiResponse);
      verify(footballClient).getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON);
    }

    @Test
    @DisplayName("delegates to FootballClient with the exact league and season arguments")
    void delegatesToClient_withCorrectArguments() {
      var apiResponse = responseWith(List.of(ANY_RESPONSE));
      when(footballClient.getStandingsForLeagueAndSeason(140, 2022)).thenReturn(apiResponse);

      fetcher.fetchValidatedStandings(140, 2022);

      verify(footballClient).getStandingsForLeagueAndSeason(140, 2022);
    }
  }

  // ---------------------------------------------------------------------------
  // No-standings scenarios
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("fetchValidatedStandings – no standings found")
  class NoStandingsScenarios {

    @Test
    @DisplayName("throws CustomNotFoundException when response list is null")
    void throwsNotFoundException_whenResponseIsNull() {
      var apiResponse = new ApiStandingsResponse("standings", null, List.of(), 0, null, null);
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      assertThatThrownBy(() -> fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON))
          .isInstanceOf(CustomNotFoundException.class)
          .hasMessageContaining(String.valueOf(LEAGUE_ID))
          .hasMessageContaining(String.valueOf(SEASON));
    }

    @Test
    @DisplayName("throws CustomNotFoundException when response list is empty")
    void throwsNotFoundException_whenResponseIsEmpty() {
      var apiResponse = responseWith(List.of());
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      assertThatThrownBy(() -> fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON))
          .isInstanceOf(CustomNotFoundException.class)
          .hasMessageContaining("No standings found for league")
          .hasMessageContaining(String.valueOf(LEAGUE_ID))
          .hasMessageContaining(String.valueOf(SEASON));
    }
  }

  // ---------------------------------------------------------------------------
  // Error-handling scenarios
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("fetchValidatedStandings – API error handling")
  class ErrorHandlingScenarios {

    @Test
    @DisplayName(
        "throws CustomTooManyRequestsException when error contains 'requests' key (rate limit)")
    void throwsTooManyRequests_whenRequestsKeyPresent() {
      var error = Map.<String, Object>of("requests", "Limit reached");
      var apiResponse = responseWithErrors(List.of(error));
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      assertThatThrownBy(() -> fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON))
          .isInstanceOf(CustomTooManyRequestsException.class)
          .hasMessageContaining("daily request limit");
    }

    @Test
    @DisplayName(
        "throws CustomTooManyRequestsException with plan message when error contains 'plan' key")
    void throwsTooManyRequests_whenPlanKeyPresent() {
      String planMsg = "You have exceeded your plan's request limit for this season.";
      var error = Map.<String, Object>of("plan", planMsg);
      var apiResponse = responseWithErrors(List.of(error));
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      assertThatThrownBy(() -> fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON))
          .isInstanceOf(CustomTooManyRequestsException.class)
          .hasMessage(planMsg);
    }

    @Test
    @DisplayName("throws CustomBadGatewayException for an unrecognised error key")
    void throwsBadGateway_whenUnknownErrorKeyPresent() {
      var error = Map.<String, Object>of("unknown_key", "some value");
      var apiResponse = responseWithErrors(List.of(error));
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      assertThatThrownBy(() -> fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON))
          .isInstanceOf(CustomBadGatewayException.class)
          .hasMessageContaining("An error occurred while processing the request");
    }

    @Test
    @DisplayName("does not throw when errors list is null (treated as no errors)")
    void doesNotThrow_whenErrorsListIsNull() {
      var apiResponse =
          new ApiStandingsResponse("standings", null, null, 0, null, List.of(ANY_RESPONSE));
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      ApiStandingsResponse result = fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON);

      assertThat(result).isEqualTo(apiResponse);
    }

    @Test
    @DisplayName("does not throw when errors list is empty (treated as no errors)")
    void doesNotThrow_whenErrorsListIsEmpty() {
      var apiResponse = responseWith(List.of(ANY_RESPONSE)); // errors = empty list
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      ApiStandingsResponse result = fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON);

      assertThat(result).isEqualTo(apiResponse);
    }

    @Test
    @DisplayName("'requests' key takes precedence over 'plan' key when both are present")
    void requestsKeyTakesPrecedence_overPlanKey() {
      // If a single error map contains both keys, `containsKey("requests")` is checked first
      var error = Map.<String, Object>of("requests", "exhausted", "plan", "some plan msg");
      var apiResponse = responseWithErrors(List.of(error));
      when(footballClient.getStandingsForLeagueAndSeason(LEAGUE_ID, SEASON))
          .thenReturn(apiResponse);

      assertThatThrownBy(() -> fetcher.fetchValidatedStandings(LEAGUE_ID, SEASON))
          .isInstanceOf(CustomTooManyRequestsException.class)
          .hasMessageContaining("daily request limit");
    }
  }
}
