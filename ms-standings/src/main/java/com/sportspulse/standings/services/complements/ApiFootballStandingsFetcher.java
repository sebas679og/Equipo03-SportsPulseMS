package com.sportspulse.standings.services.complements;

import com.sportspulse.standings.exceptions.CustomBadGatewayException;
import com.sportspulse.standings.exceptions.CustomNotFoundException;
import com.sportspulse.standings.exceptions.CustomTooManyRequestsException;
import com.sportspulse.standings.integrations.football.FootballClient;
import com.sportspulse.standings.integrations.football.dto.ApiStandingsResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ApiFootballStandingsFetcher Component responsible for fetching and validating football standings
 * from the external API through the {@link FootballClient}.
 *
 * <p>Provides a centralized mechanism to retrieve standings data, handle API-related errors, and
 * ensure that valid responses are returned to the application.
 *
 * <p>Built as a Spring-managed component with Lombok annotations {@link Slf4j} and {@link
 * RequiredArgsConstructor} for logging and constructor-based dependency injection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiFootballStandingsFetcher {

  private final FootballClient footballClient;

  /**
   * Fetches and validates league standings for a given league and season.
   *
   * <p>Delegates the request to the {@link FootballClient}, applies error handling, and ensures
   * that the response contains valid standings data. If no standings are found, a {@link
   * CustomNotFoundException} is thrown.
   *
   * @param league the unique identifier of the league
   * @param season the season year to retrieve standings for
   * @return an {@link ApiStandingsResponse} containing validated standings data
   * @throws CustomNotFoundException if no standings are found for the given league and season
   */
  public ApiStandingsResponse fetchValidatedStandings(int league, int season) {
    ApiStandingsResponse api = footballClient.getStandingsForLeagueAndSeason(league, season);

    handleApiFootballErrors(api);

    if (api.response() == null || api.response().isEmpty()) {
      throw new CustomNotFoundException(
          String.format("No standings found for league %d and season %d", league, season));
    }
    return api;
  }

  private void handleApiFootballErrors(ApiStandingsResponse api) {
    if (api.errors() == null || api.errors().isEmpty()) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> errorDetail = (Map<String, Object>) api.errors().getFirst();

    if (errorDetail.containsKey("requests")) {
      log.warn(
          "Api-Football 429 Rate Limit exceeded. "
              + "Available requests have been exhausted. Body: {}",
          errorDetail);
      throw new CustomTooManyRequestsException(
          "The daily request limit to Api-Football has been "
              + "reached, please try again tomorrow");
    } else if (errorDetail.containsKey("plan")) {
      String planMessage = (String) errorDetail.get("plan");
      log.warn(
          "Api-Football, the request limit per season has been exceeded. "
              + "Available requests have been exhausted. Body: {}",
          planMessage);
      throw new CustomTooManyRequestsException(planMessage);
    } else {
      log.error("Api-Football returned errors in the response. Body: {}", errorDetail);
      throw new CustomBadGatewayException(
          "An error occurred while processing the request to Api-Football. "
              + "Please try again later.");
    }
  }
}
