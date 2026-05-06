package com.sportspulse.leagues.integrations.football;

import com.sportspulse.leagues.exceptions.CustomBadGatewayException;
import com.sportspulse.leagues.exceptions.CustomServiceUnavailableException;
import com.sportspulse.leagues.integrations.football.dto.ApiLeagueResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

/**
 * FootballClientImpl Implementation of the {@link FootballApiClient} interface that interacts with
 * the external football API using a configured {@link WebClient}.
 *
 * <p>Provides methods to retrieve league standings and other football-related data. Built as a
 * Spring-managed component with dependency injection for the API client.
 */
@Slf4j
@Service
public class FootballApiClientImpl implements FootballApiClient {

  private final WebClient apiFootballWebClient;

  public FootballApiClientImpl(@Qualifier("apiFootballWebClient") WebClient apiFootballWebClient) {
    this.apiFootballWebClient = apiFootballWebClient;
  }

  @Override
  @Cacheable(value = "leaguesByFilters", key = "(#country ?: 'all') + '-' + (#season ?: 'all')")
  public ApiLeagueResponse getLeaguesCountryAndSeason(String country, Integer season) {
    String leagueKey =
        (country != null ? country : "all") + "-" + (season != null ? season : "all");
    WebClient.RequestHeadersSpec<?> request =
        apiFootballWebClient
            .get()
            .uri(
                uriBuilder -> {
                  uriBuilder.path("/leagues");
                  if (country != null) {
                    uriBuilder.queryParam("country", country);
                  }
                  if (season != null) {
                    uriBuilder.queryParam("season", season);
                  }

                  return uriBuilder.build();
                });
    return executeRequest(request, leagueKey);
  }

  @Override
  @Cacheable(value = "leaguesById", key = "#leagueId")
  public ApiLeagueResponse getLeagueById(int leagueId) {
    WebClient.RequestHeadersSpec<?> request =
        apiFootballWebClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/leagues").queryParam("id", leagueId).build());
    return executeRequest(request, String.valueOf(leagueId));
  }

  private ApiLeagueResponse executeRequest(
      WebClient.RequestHeadersSpec<?> request, String leagueKey) {
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
                                  + "leagues: {}, Body: {}",
                              leagueKey,
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
        .bodyToMono(ApiLeagueResponse.class)
        .onErrorMap(
            WebClientRequestException.class,
            ex -> {
              if (log.isErrorEnabled()) {
                log.error(
                    "Auth Service is unreachable. Cause: {} - {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
              }
              return new CustomBadGatewayException("Session validation service is unreachable");
            })
        .blockOptional()
        .orElseThrow(
            () -> {
              log.error(
                  "Api-Football returned an empty or null body for the requested. league: {}",
                  leagueKey);
              return new CustomServiceUnavailableException(
                  "Api-Football is not currently available, please try again");
            });
  }
}
