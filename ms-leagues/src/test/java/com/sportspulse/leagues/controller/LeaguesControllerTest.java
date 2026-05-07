package com.sportspulse.leagues.controller;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sportspulse.leagues.AbstractIntegrationTest;
import com.sportspulse.leagues.config.constants.ApiPaths;
import com.sportspulse.leagues.integrations.msauth.AuthClient;
import com.sportspulse.leagues.integrations.msauth.dto.UserResponse;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class LeaguesControllerTest extends AbstractIntegrationTest {

  private static final String VALID_TOKEN = "valid-test-token";

  private static final String TYPE_TOKEN = "Bearer ";

  private static final UserResponse VALID_USER =
      new UserResponse(
          true, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), "javier_ruiz", "USER");

  @MockitoBean private AuthClient authClient;

  @Autowired MockMvc mockMvc;

  @AfterEach
  void tearDown() {
    Mockito.reset(authClient);
  }

  @Test
  void shouldReturnLeaguesByCountry_whenValidTokenAndCountryProvided() throws Exception {
    leaguesStub.stubByCountry("spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
  }

  @Test
  void shouldReturnLeaguesBySeason_whenValidTokenAndSeasonProvided() throws Exception {
    leaguesStub.stubBySeason("2023");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("season", "2023")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
  }

  @Test
  void shouldReturnLeaguesBySeasonAndCountry_whenValidTokenAndSeasonAndCountryProvided()
      throws Exception {
    leaguesStub.stubBySeasonAndCountry("2023", "spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("season", "2023")
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
  }

  @Test
  void shouldReturnLeaguesById_whenValidTokenAndIdProvided() throws Exception {
    leaguesStub.stubById(140);

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUE_BY_ID, 140)
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(140));
  }

  @Test
  void shouldReturn503_whenValidTokenAndCountryProvided() throws Exception {
    leaguesStub.stubByCountry_Return204("spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn503_whenValidTokenAndCountryProvided_ErrorApiFootball499() throws Exception {
    leaguesStub.stubByCountry_Return499("spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn503_whenValidTokenAndCountryProvided_ErrorApiFootball500() throws Exception {
    leaguesStub.stubByCountry_Return500("spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn503_whenValidTokenAndCountryProvided_EmptyResponse() throws Exception {
    leaguesStub.stubByCountry_ReturnEmpty("spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.code").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn429_whenValidTokenAndCountryProvided_ErrorPlan() throws Exception {
    leaguesStub.stubByCountry_ReturnErrorPlan("spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.code").value(HttpStatus.TOO_MANY_REQUESTS.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn429_whenValidTokenAndCountryProvided_ErrorRequests() throws Exception {
    leaguesStub.stubByCountry_ReturnErrorRequests("spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.code").value(HttpStatus.TOO_MANY_REQUESTS.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn429_whenValidTokenAndCountryProvided_Errors() throws Exception {
    leaguesStub.stubByCountry_ReturnErrors("spain");

    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

    mockMvc
        .perform(
            get(ApiPaths.Leagues.LEAGUES_BY_FILTER)
                .param("country", "spain")
                .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + VALID_TOKEN))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_GATEWAY.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_GATEWAY.getReasonPhrase()))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
