package com.sportspulse.leagues.integration.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.leagues.LeaguesTestDataProvider;
import com.sportspulse.leagues.config.properties.FootballApiProperties;
import com.sportspulse.leagues.exceptions.ExternalApiException;
import com.sportspulse.leagues.integration.dto.ApiFootballLeagueWrapper;
import com.sportspulse.leagues.integration.dto.ApiFootballLeaguesEnvelope;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("FootballApiClientImpl Tests")
class FootballApiClientImplTest {

  @Mock private RestTemplate restTemplate;

  @Mock private FootballApiProperties footballApiProperties;

  @InjectMocks private FootballApiClientImpl footballApiClient;

  @Test
  @DisplayName("getLeagues should call API-Football with expected query params")
  void getLeagues_shouldBuildUriAndReturnResponse() {
    ApiFootballLeagueWrapper wrapper = LeaguesTestDataProvider.apiLeagueWrapper();
    ApiFootballLeaguesEnvelope envelope = new ApiFootballLeaguesEnvelope(List.of(wrapper));

    when(footballApiProperties.getBaseUrl()).thenReturn("https://v3.football.api-sports.io");
    when(footballApiProperties.getKey()).thenReturn("test-api-key");
    when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(ApiFootballLeaguesEnvelope.class)))
        .thenReturn(ResponseEntity.ok(envelope));

    List<ApiFootballLeagueWrapper> response = footballApiClient.getLeagues("Spain", 2024, 140);

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    verify(restTemplate)
        .exchange(
            uriCaptor.capture(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(ApiFootballLeaguesEnvelope.class));

    assertThat(uriCaptor.getValue().toString())
        .contains("/leagues")
        .contains("country=Spain")
        .contains("season=2024")
        .contains("id=140");
    assertThat(response).hasSize(1);
    assertThat(response.getFirst().getLeague().getId()).isEqualTo(140);
  }

  @Test
  @DisplayName("getLeagues should wrap RestClientException as ExternalApiException")
  void getLeagues_shouldThrowExternalApiException() {
    when(footballApiProperties.getBaseUrl()).thenReturn("https://v3.football.api-sports.io");
    when(footballApiProperties.getKey()).thenReturn("test-api-key");
    when(restTemplate.exchange(
            any(URI.class),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(ApiFootballLeaguesEnvelope.class)))
        .thenThrow(new RestClientException("boom"));

    assertThatThrownBy(() -> footballApiClient.getLeagues("Spain", 2024, null))
        .isInstanceOf(ExternalApiException.class)
        .hasMessageContaining("Error al consultar API-Football");
  }
}
