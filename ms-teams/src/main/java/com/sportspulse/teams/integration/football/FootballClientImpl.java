package com.sportspulse.teams.integration.football;

import com.sportspulse.teams.exceptions.CustomServiceUnavailableException;
import com.sportspulse.teams.integration.football.dto.teamid.ApiFootballTeamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * FootballClientImpl Implementation of the {@link FootballClient} interface. Provides methods to
 * interact with the Football API and retrieve team-related information.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FootballClientImpl implements FootballClient {

  @Qualifier("apiFootballWebClient")
  private final WebClient apiFootballWebClient;

  @Override
  @Cacheable(value = "teams", key = "#teamId")
  public ApiFootballTeamResponse getApiFootballTeamById(int teamId) {
    WebClient.RequestHeadersSpec<?> request =
        apiFootballWebClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/teams").queryParam("id", teamId).build());

    return executeRequest(request, String.valueOf(teamId));
  }

  @Override
  @Cacheable(value = "teamsByLeagueAndSeason", key = "#leagueId + '-' + #season")
  public ApiFootballTeamResponse getApiFootballTeamByLeagueAndSeason(int leagueId, int season) {
    WebClient.RequestHeadersSpec<?> request =
        apiFootballWebClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/teams")
                        .queryParam("league", leagueId)
                        .queryParam("season", season)
                        .build());
    return executeRequest(request, leagueId + "-" + season);
  }

  private ApiFootballTeamResponse executeRequest(
      WebClient.RequestHeadersSpec<?> request, String teamId) {
    return request
        .retrieve()
        .onStatus(
            status -> status.value() == 204,
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("No body")
                    .flatMap(
                        body -> {
                          log.error(
                              "Api-Football 204 No Content. The requested team Api-Football error. "
                                  + "Team: {}, Body: {}",
                              teamId,
                              response);
                          return Mono.error(
                              new CustomServiceUnavailableException(
                                  "Api-Football is not currently available, please try again"));
                        }))
        .onStatus(
            status -> status.value() == 499 || status.value() == 500,
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("No body")
                    .flatMap(
                        body -> {
                          if (log.isErrorEnabled()) {
                            log.error(
                                "Api-Football error. Status: {}, Body: {}",
                                response.statusCode(),
                                body);
                          }
                          return Mono.error(
                              new CustomServiceUnavailableException(
                                  "Api-Football is not currently available, please try again"));
                        }))
        .bodyToMono(ApiFootballTeamResponse.class)
        .blockOptional()
        .orElseThrow(
            () -> {
              log.error(
                  "Api-Football returned an empty or null body for the requested. Team: {}",
                  teamId);
              return new CustomServiceUnavailableException(
                  "Api-Football is not currently available, please try again");
            });
  }
}
