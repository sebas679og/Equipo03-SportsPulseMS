package com.sportspulse.leagues.integration.client;

import com.sportspulse.leagues.config.constants.InternalHeaders;
import com.sportspulse.leagues.config.properties.FootballApiProperties;
import com.sportspulse.leagues.exceptions.ExternalApiException;
import com.sportspulse.leagues.integration.dto.ApiFootballLeagueWrapper;
import com.sportspulse.leagues.integration.dto.ApiFootballLeaguesEnvelope;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/** RestTemplate implementation for API-Football calls. */
@Component
@RequiredArgsConstructor
@Slf4j
public class FootballApiClientImpl implements FootballApiClient {

  private final RestTemplate restTemplate;
  private final FootballApiProperties footballApiProperties;

  @Override
  public List<ApiFootballLeagueWrapper> getLeagues(
      String country, Integer season, Integer leagueId) {
    URI uri = buildLeaguesUri(country, season, leagueId);
    HttpEntity<Void> request = new HttpEntity<>(buildHeaders());

    try {
      if (log.isInfoEnabled()) {
        log.info(
            "Calling API-Football leagues endpoint - uri='{}' country='{}' season='{}' "
                + "leagueId='{}'",
            uri,
            country,
            season,
            leagueId);
      }

      ResponseEntity<ApiFootballLeaguesEnvelope> response =
          restTemplate.exchange(uri, HttpMethod.GET, request, ApiFootballLeaguesEnvelope.class);

      return mapLeaguesResponse(response, uri);
    } catch (RestClientException ex) {
      if (log.isErrorEnabled()) {
        log.error(
            "API-Football request failed - uri='{}' - error='{}'", uri, ex.getMessage(), ex);
      }
      throw new ExternalApiException("Error al consultar API-Football", ex);
    }
  }

  private List<ApiFootballLeagueWrapper> mapLeaguesResponse(
      ResponseEntity<ApiFootballLeaguesEnvelope> response, URI uri) {
    ApiFootballLeaguesEnvelope body = response.getBody();

    if (body == null || body.getResponse() == null) {
      if (log.isWarnEnabled()) {
        log.warn("API-Football returned empty body or response list - uri='{}'", uri);
      }
      return List.of();
    }

    if (log.isInfoEnabled()) {
      log.info(
          "API-Football leagues retrieved successfully - uri='{}' size={}",
          uri,
          body.getResponse().size());
    }

    return body.getResponse();
  }

  private URI buildLeaguesUri(String country, Integer season, Integer leagueId) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(footballApiProperties.getBaseUrl()).path("/leagues");

    if (StringUtils.hasText(country)) {
      builder.queryParam("country", country);
    }
    if (season != null) {
      builder.queryParam("season", season);
    }
    if (leagueId != null) {
      builder.queryParam("id", leagueId);
    }
    return builder.build(true).toUri();
  }

  private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.set(InternalHeaders.API_SPORTS_KEY, footballApiProperties.getKey());
    return headers;
  }
}
