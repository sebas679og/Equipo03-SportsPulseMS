package com.sportspulse.fixtures.client;

import static com.sportspulse.fixtures.constants.CacheConstants.FIXTURES_CACHE;
import static com.sportspulse.fixtures.constants.CacheConstants.FIXTURES_EVENTS_CACHE;
import static com.sportspulse.fixtures.constants.CacheConstants.FIXTURES_LIVE_CACHE;

import com.sportspulse.fixtures.constants.ApiPaths;
import com.sportspulse.fixtures.constants.ErrorConstants;
import com.sportspulse.fixtures.constants.HttpHeaders;
import com.sportspulse.fixtures.dto.external.FootballFixtureItem;
import com.sportspulse.fixtures.dto.external.FootballFixtureResponse;
import com.sportspulse.fixtures.dto.external.event.FootballFixtureEventItem;
import com.sportspulse.fixtures.dto.external.event.FootballFixtureEventsResponse;
import com.sportspulse.fixtures.dto.external.live.FootballLiveItem;
import com.sportspulse.fixtures.dto.external.live.FootballLiveResponse;
import com.sportspulse.fixtures.dto.request.FixtureFilterRequest;
import com.sportspulse.fixtures.exceptions.FixtureNotFoundException;
import com.sportspulse.fixtures.exceptions.ServiceUnavailableException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client component for interacting with the external Football API.
 *
 * <p>Provides methods to fetch fixtures, match events, and live scores. Results are cached to
 * optimize performance and reduce external API calls.
 */
@Slf4j
@Component
public class FootballClient {

  private final WebClient footballClient;

  public FootballClient(@Qualifier("footballWebClient") WebClient footballClient) {
    this.footballClient = footballClient;
  }

  /**
   * Retrieves a list of fixtures based on provided filters (league, team, date, status).
   *
   * @param request the {@link FixtureFilterRequest} containing query parameters.
   * @return a list of {@link FootballFixtureItem}.
   * @throws FixtureNotFoundException if the API returns a 204 No Content status.
   * @throws ServiceUnavailableException if the external API returns a 5xx error.
   */
  @Cacheable(value = FIXTURES_CACHE, key = "#request")
  public List<FootballFixtureItem> getFixtures(FixtureFilterRequest request) {
    return footballClient
        .get()
        .uri(
            uriBuilder -> {
              uriBuilder.path(ApiPaths.ApiFootball.FIXTURES);
              if (request.league() != null) {
                uriBuilder.queryParam(HttpHeaders.ApiFootball.Params.LEAGUE, request.league());
              }
              if (request.team() != null) {
                uriBuilder.queryParam(HttpHeaders.ApiFootball.Params.TEAM, request.team());
              }
              if (request.date() != null) {
                uriBuilder.queryParam(HttpHeaders.ApiFootball.Params.DATE, request.date());
              }
              if (request.status() != null) {
                uriBuilder.queryParam(HttpHeaders.ApiFootball.Params.STATUS, request.status());
              }
              return uriBuilder.build();
            })
        .retrieve()
        .onStatus(
            status -> status.value() == 204,
            response -> handleNotFound("fixtures: " + request.date() + " " + request.status()))
        .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
        .bodyToMono(FootballFixtureResponse.class)
        .map(FootballFixtureResponse::response)
        .block();
  }

  /**
   * Retrieves all events (goals, cards, substitutions) for a specific fixture. Events are
   * automatically sorted by elapsed time.
   *
   * @param fixtureId the unique identifier of the fixture.
   * @return a sorted list of {@link FootballFixtureEventItem}.
   * @throws FixtureNotFoundException if no events are found for the given ID.
   */
  @Cacheable(value = FIXTURES_EVENTS_CACHE, key = "#fixtureId")
  public List<FootballFixtureEventItem> getFixtureEvents(Long fixtureId) {
    return footballClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(ApiPaths.ApiFootball.FIXTURES_EVENTS)
                    .queryParam(HttpHeaders.ApiFootball.Params.FIXTURE, fixtureId)
                    .build())
        .retrieve()
        .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
        .bodyToMono(FootballFixtureEventsResponse.class)
        .map(response -> validateAndSortEvents(response.response(), fixtureId))
        .block();
  }

  /**
   * Retrieves all currently live football fixtures. Filters the results to only include matches
   * with a valid live status.
   *
   * @return a list of {@link FootballLiveItem}, or an empty list if no matches are live.
   */
  @Cacheable(value = FIXTURES_LIVE_CACHE, unless = "#result.isEmpty()")
  public List<FootballLiveItem> getFixtureLive() {

    return footballClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(ApiPaths.ApiFootball.FIXTURES)
                    .queryParam(
                        HttpHeaders.ApiFootball.Params.LIVE, HttpHeaders.ApiFootball.Values.ALL)
                    .build())
        .retrieve()
        .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
        .bodyToMono(FootballLiveResponse.class)
        .map(
            response ->
                response.response().stream()
                    .filter(
                        item ->
                            HttpHeaders.LIVE_STATUS.contains(item.fixture().status().shortStatus()))
                    .toList())
        .defaultIfEmpty(Collections.emptyList())
        .block();
  }

  /**
   * Validates that the event list is not empty and sorts events chronologically.
   *
   * @param items the raw list of events from the API.
   * @param fixtureId the fixture ID for error context.
   * @return a sorted list of events.
   * @throws FixtureNotFoundException if items is null or empty.
   */
  private List<FootballFixtureEventItem> validateAndSortEvents(
      List<FootballFixtureEventItem> items, Long fixtureId) {
    if (items == null || items.isEmpty()) {
      throw new FixtureNotFoundException(ErrorConstants.Message.NO_FIXTURE_FOUND + fixtureId);
    }
    return items.stream().sorted(Comparator.comparing(item -> item.time().elapsed())).toList();
  }

  /**
   * Handles 204 No Content scenarios by mapping them to a domain exception.
   *
   * @param context descriptive information about the failed request.
   * @return a {@link Mono} emitting a {@link FixtureNotFoundException}.
   */
  private Mono<? extends Throwable> handleNotFound(String context) {
    log.warn("Api-Football 204 No Content for: {}", context);
    return Mono.error(new FixtureNotFoundException(ErrorConstants.Message.NO_DATA_FOUND + context));
  }

  /**
   * Handles 5xx Server Errors from the external API.
   *
   * @param response the {@link ClientResponse} from the server.
   * @return a {@link Mono} emitting a {@link ServiceUnavailableException}.
   */
  private Mono<? extends Throwable> handleServerError(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .defaultIfEmpty("No body")
        .flatMap(
            body -> {
              log.error("Api-Football error. Status: {}, Body: {}", response.statusCode(), body);
              return Mono.error(
                  new ServiceUnavailableException(
                      ErrorConstants.Message.API_FOOTBALL_NOT_AVAILABLE));
            });
  }
}
