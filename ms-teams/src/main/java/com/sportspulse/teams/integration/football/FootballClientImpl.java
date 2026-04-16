package com.sportspulse.teams.integration.football;

import com.sportspulse.teams.exceptions.CustomNotFoundException;
import com.sportspulse.teams.exceptions.CustomServiceUnavailableException;
import com.sportspulse.teams.exceptions.CustomTooManyRequestsException;
import com.sportspulse.teams.integration.football.dto.teamid.ApiFootballTeamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
  public ApiFootballTeamResponse getApiFootballTeamById(int teamId) {
    WebClient.RequestHeadersSpec<?> request =
        apiFootballWebClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/teams").queryParam("id", teamId).build());

    return executeRequest(request, ApiFootballTeamResponse.class, teamId);
  }

  private <T> T executeRequest(
      WebClient.RequestHeadersSpec<?> request, Class<T> responseType, int teamId) {
    return request
        .retrieve()
        .onStatus(
            status -> status.value() == 204,
            response -> {
              log.warn(
                  "Api-Football 204 No Content. The requested team was not found. Team: {}",
                  teamId);
              return Mono.error(
                  new CustomNotFoundException("The requested team was not found in Api-Football"));
            })
        .onStatus(
            status -> status.value() == 429,
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("No body")
                    .flatMap(
                        body -> {
                          log.warn(
                              "Api-Football 429 Rate Limit exceeded. "
                                  + "Available requests have been exhausted. Body: {}",
                              body);
                          return Mono.error(
                              new CustomTooManyRequestsException(
                                  "The daily request limit to Api-Football has been "
                                      + "reached, please try again tomorrow"));
                        }))
        .onStatus(
            status -> status.value() == 499 || status.value() == 500,
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("No body")
                    .flatMap(
                        body -> {
                          log.error(
                              "Api-Football error. Status: {}, Body: {}",
                              response.statusCode(),
                              body);
                          return Mono.error(
                              new CustomServiceUnavailableException(
                                  "Api-Football is not currently available, please try again"));
                        }))
        .bodyToMono(responseType)
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
