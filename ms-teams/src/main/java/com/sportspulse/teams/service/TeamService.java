package com.sportspulse.teams.service;

import com.sportspulse.teams.constants.ApiPaths;
import com.sportspulse.teams.constants.Errors;
import com.sportspulse.teams.dto.external.ApiFootballResponse;
import com.sportspulse.teams.dto.response.TeamResponse;
import com.sportspulse.teams.mappers.TeamMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Service responsible for retrieving football team data from the API-Football external service.
 *
 * <p>Fetches raw team and venue data, transforms it into the internal {@link TeamResponse} format
 * and returns it to the controller layer.
 */
@Service
@RequiredArgsConstructor
public class TeamService {

  private final WebClient footballWebClient;
  private final TeamMapper teamMapper;

  /**
   * Retrieves the list of teams participating in a league during a specific season.
   *
   * @param league the league identifier (e.g. 39 for Premier League).
   * @param season the season year (e.g. 2023).
   * @return a list of {@link TeamResponse} with team and venue details.
   * @throws WebClientResponseException if the API-Football service returns an error.
   * @throws IllegalStateException if the API-Football response body is empty.
   */
  public List<TeamResponse> getTeams(Integer league, Integer season) {
    ApiFootballResponse response =
        footballWebClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(ApiPaths.ApiFootball.TEAMS_PATH)
                        .queryParam(ApiPaths.ApiFootball.LEAGUE_PARAM, league)
                        .queryParam(ApiPaths.ApiFootball.SEASON_PARAM, season)
                        .build())
            .retrieve()
            .bodyToMono(ApiFootballResponse.class)
            .blockOptional()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        Errors.Message.EXTERNAL_SERVICE_ERROR + "empty response body."));

    return response.teams().stream().map(teamMapper::toTeamResponse).toList();
  }
}
