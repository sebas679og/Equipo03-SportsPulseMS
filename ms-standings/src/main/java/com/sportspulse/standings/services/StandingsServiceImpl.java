package com.sportspulse.standings.services;

import com.sportspulse.standings.dtos.request.LeagueAndSeasonRequest;
import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;
import com.sportspulse.standings.exceptions.CustomBadGatewayException;
import com.sportspulse.standings.exceptions.CustomNotFoundException;
import com.sportspulse.standings.exceptions.CustomTooManyRequestsException;
import com.sportspulse.standings.integrations.football.FootballClient;
import com.sportspulse.standings.integrations.football.dto.ApiLeague;
import com.sportspulse.standings.integrations.football.dto.ApiStandingsResponse;
import com.sportspulse.standings.utils.mappers.StandingsMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * StandingsServiceImpl Implementation of the {@link StandingsService} interface that provides
 * access to league standings data.
 *
 * <p>Acts as a Spring-managed service component, delegating requests to underlying clients or
 * mappers to retrieve and transform football standings information. Built with Lombok annotations
 * {@link Slf4j} and {@link RequiredArgsConstructor} for logging support and constructor-based
 * dependency injection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StandingsServiceImpl implements StandingsService {

  private final FootballClient footballClient;
  private final StandingsMapper standingsMapper;

  @Override
  public StandingsLeagueAndSeasonResponse getStandingsByLeagueAndSeason(
      LeagueAndSeasonRequest request) {

    ApiStandingsResponse api =
        footballClient.getStandingsForLeagueAndSeason(
            request.getLeague(), Integer.parseInt(request.getSeason()));

    handleApiFootballErrors(api);

    if (api.response() == null || api.response().isEmpty()) {
      throw new CustomNotFoundException(
          String.format(
              "No standings found for league %d and season %s",
              request.getLeague(), request.getSeason()));
    }

    ApiLeague apiLeague = api.response().getFirst().league();

    return standingsMapper.toResponseFromLeague(apiLeague);
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
