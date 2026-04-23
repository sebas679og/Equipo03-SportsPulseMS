package com.sportspulse.standings.integrations.football;

import com.sportspulse.standings.exceptions.CustomServiceUnavailableException;
import com.sportspulse.standings.integrations.football.dto.ApiStandingsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * FootballClientImpl Implementation of the {@link FootballClient} interface that interacts with the
 * external football API using a configured {@link WebClient}.
 *
 * <p>Provides methods to retrieve league standings and other football-related data. Built as a
 * Spring-managed component with dependency injection for the API client.
 */
@Slf4j
@Component
public class FootballClientImpl implements FootballClient {

  private final WebClient apiFootballWebClient;

  public FootballClientImpl(@Qualifier("apiFootballWebClient") WebClient apiFootballWebClient) {
    this.apiFootballWebClient = apiFootballWebClient;
  }

  @Override
  @Cacheable(value = "standingsLeagueAndSeason", key = "#leagueId + '-' + #season")
  public ApiStandingsResponse getStandingsForLeagueAndSeason(int leagueId, int season) {
    WebClient.RequestHeadersSpec<?> request =
        apiFootballWebClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/standings")
                        .queryParam("league", leagueId)
                        .queryParam("season", season)
                        .build());
    return executeRequest(request, leagueId + "-" + season);
  }

  private ApiStandingsResponse executeRequest(
      WebClient.RequestHeadersSpec<?> request, String standingKey) {
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
                              "Api-Football 204 No Content. "
                                  + "The requested standing Api-Football error. "
                                  + "standings: {}, Body: {}",
                              standingKey,
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
        .bodyToMono(ApiStandingsResponse.class)
        .blockOptional()
        .orElseThrow(
            () -> {
              log.error(
                  "Api-Football returned an empty or null body for the requested. standing: {}",
                  standingKey);
              return new CustomServiceUnavailableException(
                  "Api-Football is not currently available, please try again");
            });
  }
}
