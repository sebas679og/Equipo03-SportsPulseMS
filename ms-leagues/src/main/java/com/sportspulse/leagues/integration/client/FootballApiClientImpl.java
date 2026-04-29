package com.sportspulse.leagues.integration.client;

import com.sportspulse.leagues.exceptions.ExternalApiException;
import com.sportspulse.leagues.integration.dto.ApiFootballLeagueWrapper;
import com.sportspulse.leagues.integration.dto.ApiFootballLeaguesEnvelope;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** WebClient implementation for API-Football calls. */
@Component
@RequiredArgsConstructor
@Slf4j
public class FootballApiClientImpl implements FootballApiClient {

  @Qualifier("apiFootballWebClient")
  private final WebClient apiFootballWebClient;

  @Override
  @Cacheable(
      value = "leagues",
      key =
          "T(String).valueOf(#country) + ':' + T(String).valueOf(#season) + ':' + T(String).valueOf(#leagueId)")
  public List<ApiFootballLeagueWrapper> getLeagues(
      String country, Integer season, Integer leagueId) {
    try {
      ApiFootballLeaguesEnvelope response =
          apiFootballWebClient
              .get()
              .uri(
                  uriBuilder -> {
                    uriBuilder.path("/leagues");
                    if (country != null && !country.isBlank()) {
                      uriBuilder.queryParam("country", country);
                    }
                    if (season != null) {
                      uriBuilder.queryParam("season", season);
                    }
                    if (leagueId != null) {
                      uriBuilder.queryParam("id", leagueId);
                    }
                    return uriBuilder.build();
                  })
              .accept(MediaType.APPLICATION_JSON)
              .retrieve()
              .onStatus(
                  status -> !status.equals(HttpStatus.OK),
                  clientResponse ->
                      clientResponse
                          .bodyToMono(String.class)
                          .defaultIfEmpty("No body")
                          .flatMap(
                              body ->
                                  Mono.error(
                                      new ExternalApiException(
                                          "Error HTTP al consultar API-Football: "
                                              + clientResponse.statusCode().value(),
                                          new IllegalStateException(body)))))
              .bodyToMono(ApiFootballLeaguesEnvelope.class)
              .blockOptional()
              .orElseThrow(
                  () ->
                      new ExternalApiException(
                          "API-Football devolvió body vacío",
                          new IllegalStateException("Empty response body")));

      return mapLeaguesResponse(response);
    } catch (ExternalApiException ex) {
      throw ex;
    } catch (Exception ex) {
      if (log.isErrorEnabled()) {
        log.error(
            "API-Football request failed - country='{}' season='{}' leagueId='{}' - error='{}'",
            country,
            season,
            leagueId,
            ex.getMessage(),
            ex);
      }
      throw new ExternalApiException("Error al consultar API-Football", ex);
    }
  }

  private List<ApiFootballLeagueWrapper> mapLeaguesResponse(ApiFootballLeaguesEnvelope body) {
    if (body.getResponse() == null) {
      if (log.isWarnEnabled()) {
        log.warn("API-Football returned response list as null");
      }
      return List.of();
    }

    if (log.isInfoEnabled()) {
      log.info("API-Football leagues retrieved successfully - size={}", body.getResponse().size());
    }

    return body.getResponse();
  }
}
