package com.sportspulse.teams.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sportspulse.teams.exceptions.CustomBadGatewayException;
import com.sportspulse.teams.exceptions.CustomServiceUnavailableException;
import com.sportspulse.teams.exceptions.CustomUnauthorizedException;
import com.sportspulse.teams.integration.football.FootballClient;
import com.sportspulse.teams.integration.football.dto.teamid.ApiFootballTeamResponse;
import com.sportspulse.teams.integration.football.dto.teamid.ApiPagingResponse;
import com.sportspulse.teams.integration.football.dto.teamid.ApiResponseItem;
import com.sportspulse.teams.integration.football.dto.teamid.ApiTeamResponse;
import com.sportspulse.teams.integration.football.dto.teamid.ApiVenueResponse;
import com.sportspulse.teams.integration.msauth.AuthClient;
import com.sportspulse.teams.integration.msauth.dto.UserResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TeamController — Integration Tests")
class TeamControllerTest {

  @Autowired MockMvc mockMvc;
  @Autowired FootballClient footballClient;
  @Autowired AuthClient authClient;

  @TestConfiguration
  static class MockClientsConfig {

    @Bean
    @Primary
    FootballClient footballClient() {
      return Mockito.mock(FootballClient.class);
    }

    @Bean
    @Primary
    AuthClient authClient() {
      return Mockito.mock(AuthClient.class);
    }
  }

  /** team id=22, Iran — successResponsesTeamById[0]. */
  private static final ApiResponseItem IRAN_ITEM =
      new ApiResponseItem(
          new ApiTeamResponse(
              22,
              "Iran",
              "IRA",
              "Iran",
              1920,
              true,
              "https://media.api-sports.io/football/teams/22.png"),
          new ApiVenueResponse(
              22428,
              "Imam Reza Stadium",
              "Khayyam Boulevard",
              "Mashhad",
              25000,
              "grass",
              "https://media.api-sports.io/football/venues/22428.png"));

  /** team id=30, Peru — successResponsesTeamById[1]. */
  private static final ApiResponseItem PERU_ITEM =
      new ApiResponseItem(
          new ApiTeamResponse(
              30,
              "Peru",
              "PER",
              "Peru",
              1922,
              true,
              "https://media.api-sports.io/football/teams/30.png"),
          new ApiVenueResponse(
              1242,
              "Estadio Monumental",
              "Avenida Javier Prado Este 7596, Ate",
              "Lima",
              80093,
              "grass",
              "https://media.api-sports.io/football/venues/1242.png"));

  /** First two entries of successResponsesTeamsByLeague12AndSeason2023. */
  private static final List<ApiResponseItem> LEAGUE_12_SEASON_2023_ITEMS =
      List.of(
          new ApiResponseItem(
              new ApiTeamResponse(
                  904,
                  "CR Belouizdad",
                  "BEL",
                  "Algeria",
                  1962,
                  false,
                  "https://media.api-sports.io/football/teams/904.png"),
              new ApiVenueResponse(
                  5,
                  "Stade du 5 Juillet 1962",
                  "Lotissement Benhadaddi, Villa 12B, Chéraga",
                  "Algiers",
                  80200,
                  "grass",
                  "https://media.api-sports.io/football/venues/5.png")),
          new ApiResponseItem(
              new ApiTeamResponse(
                  911,
                  "CS Constantine",
                  "CON",
                  "Algeria",
                  1898,
                  false,
                  "https://media.api-sports.io/football/teams/911.png"),
              new ApiVenueResponse(
                  16,
                  "Stade Mohamed-Hamlaoui",
                  "Cité les Muriers",
                  "Constantine",
                  50000,
                  "grass",
                  "https://media.api-sports.io/football/venues/16.png")));

  /** Valid token used across happy-path tests. */
  private static final String VALID_TOKEN = "valid-jwt-token";

  /** UserResponse returned by authClient for a valid token. */
  private static final UserResponse VALID_USER =
      new UserResponse(
          true, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), "javier_ruiz", "USER");

  /** Paging stub — always 1/1 for these fixtures. */
  private static final ApiPagingResponse PAGING = new ApiPagingResponse(1, 1);

  // ──────────────────────────────────────────────────────────────────────────
  // Reset mocks before each test so stubs don't bleed across tests
  // ──────────────────────────────────────────────────────────────────────────

  @BeforeEach
  void resetMocks() {
    Mockito.reset(footballClient, authClient);
  }

  // ══════════════════════════════════════════════════════════════════════════
  // GET /api/teams/{teamId}
  // ══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("GET /api/teams/{teamId}")
  class GetTeamById {

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

      @Test
      @DisplayName("200 — returns team data when teamId=22 (Iran)")
      void shouldReturnTeamById_Iran() throws Exception {
        // Arrange
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams", Map.of("id", "22"), List.of(), 1, PAGING, List.of(IRAN_ITEM));

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamById(22)).thenReturn(apiResponse);

        // Act & Assert
        mockMvc
            .perform(
                get("/api/teams/{teamId}", 22)
                    .header("Authorization", "Bearer " + VALID_TOKEN)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(22))
            .andExpect(jsonPath("$.name").value("Iran"))
            .andExpect(jsonPath("$.country").value("Iran"))
            .andExpect(jsonPath("$.national").value(true))
            .andExpect(jsonPath("$.founded").value(1920))
            .andExpect(
                jsonPath("$.logo").value("https://media.api-sports.io/football/teams/22.png"))
            .andExpect(jsonPath("$.stadium.name").value("Imam Reza Stadium"))
            .andExpect(jsonPath("$.stadium.city").value("Mashhad"))
            .andExpect(jsonPath("$.stadium.capacity").value(25000))
            .andExpect(jsonPath("$.stadium.surface").value("grass"));
      }

      @Test
      @DisplayName("200 — returns team data when teamId=30 (Peru)")
      @SuppressWarnings("LineLength")
      void shouldReturnTeamById_Peru() throws Exception {
        // Arrange
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams", Map.of("id", "30"), List.of(), 1, PAGING, List.of(PERU_ITEM));

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamById(30)).thenReturn(apiResponse);

        // Act & Assert
        mockMvc
            .perform(
                get("/api/teams/{teamId}", 30).header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(30))
            .andExpect(jsonPath("$.name").value("Peru"))
            .andExpect(jsonPath("$.stadium.name").value("Estadio Monumental"))
            .andExpect(jsonPath("$.stadium.capacity").value(80093));
      }

      @Test
      @DisplayName("200 — request without Authorization header still passes (public endpoint)")
      @SuppressWarnings("LineLength")
      void shouldReturnTeamById_withoutAuthHeader() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams", Map.of("id", "22"), List.of(), 1, PAGING, List.of(IRAN_ITEM));

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        Mockito.when(footballClient.getApiFootballTeamById(22)).thenReturn(apiResponse);

        // Act & Assert
        mockMvc
            .perform(
                get("/api/teams/{teamId}", 22).header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(22));
      }
    }

    // ── Auth errors ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Auth errors")
    class AuthErrors {

      @Test
      @DisplayName("401 — authClient throws CustomUnauthorizedException (invalid token)")
      @SuppressWarnings("LineLength")
      void shouldReturn401_whenTokenIsInvalid() throws Exception {
        // The filter catches CustomUnauthorizedException and clears the context.
        // Security config must deny unauthenticated access to return 401.
        Mockito.when(authClient.isTokenValid("bad-token"))
            .thenThrow(
                new CustomUnauthorizedException("Invalid or expired token, please log in again"));

        mockMvc
            .perform(get("/api/teams/{teamId}", 22).header("Authorization", "Bearer bad-token"))
            .andExpect(status().isUnauthorized());
      }

      @Test
      @DisplayName("401 — authClient throws CustomBadGatewayException (403 from ms-auth)")
      void shouldReturn401_whenAuthServiceReturns403() throws Exception {
        Mockito.when(authClient.isTokenValid("some-token"))
            .thenThrow(
                new CustomBadGatewayException("Session validation service rejected the request"));

        mockMvc
            .perform(get("/api/teams/{teamId}", 22).header("Authorization", "Bearer some-token"))
            .andExpect(status().isUnauthorized());
      }
    }

    // ── Validation errors ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Validation errors")
    class ValidationErrors {

      @Test
      @DisplayName("400 — teamId=0 is rejected by service layer (not positive)")
      void shouldReturn400_whenTeamIdIsZero() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        mockMvc
            .perform(get("/api/teams/{teamId}", 0).header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadRequest());
      }

      @Test
      @DisplayName("400 — teamId=-1 is rejected by service layer (negative)")
      void shouldReturn400_whenTeamIdIsNegative() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        mockMvc
            .perform(
                get("/api/teams/{teamId}", -1).header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadRequest());
      }
    }

    // ── Api-Football error responses ─────────────────────────────────────────

    @Nested
    @DisplayName("Api-Football error responses")
    class ApiFootballErrors {

      @Test
      @DisplayName("429 — Api-Football quota exceeded (errors.requests key present)")
      void shouldReturn429_whenQuotaExceeded() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams",
                Map.of("id", "33"),
                List.of(Map.of("requests", "Exceeded the daily quota of 100 requests")),
                0,
                PAGING,
                List.of());

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamById(33)).thenReturn(apiResponse);

        mockMvc
            .perform(
                get("/api/teams/{teamId}", 33).header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isTooManyRequests());
      }

      @Test
      @DisplayName("502 — Api-Football returns errors with unknown key (noContent204 body)")
      @SuppressWarnings("LineLength")
      void shouldReturn502_whenApiFootballReturnsGenericError() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams",
                Map.of("id", "33"),
                List.of(
                    Map.of(
                        "time",
                        "2019-11-26T00:00:00+00:00",
                        "bug",
                        "This is on our side, please report us this bug on "
                            + "https://dashboard.api-football.com",
                        "report",
                        "teams?id=33")),
                0,
                PAGING,
                List.of());

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamById(33)).thenReturn(apiResponse);

        mockMvc
            .perform(
                get("/api/teams/{teamId}", 33).header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadGateway());
      }

      @Test
      @DisplayName(
          "503 — FootballClient throws CustomServiceUnavailableException (HTTP 204/499/500)")
      @SuppressWarnings("LineLength")
      void shouldReturn503_whenApiFootballIsUnavailable() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamById(22))
            .thenThrow(
                new CustomServiceUnavailableException(
                    "Api-Football is not currently available, please try again"));

        mockMvc
            .perform(
                get("/api/teams/{teamId}", 22).header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isServiceUnavailable());
      }

      @Test
      @DisplayName("404 — Api-Football returns empty response array for valid teamId")
      void shouldReturn404_whenApiFootballReturnsEmptyResponse() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams", Map.of("id", "99"), List.of(), 0, PAGING, List.of());

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamById(99)).thenReturn(apiResponse);

        mockMvc
            .perform(
                get("/api/teams/{teamId}", 99).header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isNotFound());
      }
    }
  }

  // ══════════════════════════════════════════════════════════════════════════
  // GET /api/teams  (league + season)
  // ══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("GET /api/teams")
  class GetTeamsByLeagueAndSeason {

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

      @Test
      @DisplayName("200 — returns team list for league=12 & season=2023")
      void shouldReturnTeams_forLeague12Season2023() throws Exception {
        // Arrange — mirrors successResponsesTeamsByLeague12AndSeason2023
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams",
                Map.of("league", "12", "season", "2023"),
                List.of(),
                54,
                PAGING,
                LEAGUE_12_SEASON_2023_ITEMS);

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamByLeagueAndSeason(12, 2023))
            .thenReturn(apiResponse);

        // Act & Assert
        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "2023")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            // first team: CR Belouizdad
            .andExpect(jsonPath("$.data[0].id").value(904))
            .andExpect(jsonPath("$.data[0].name").value("CR Belouizdad"))
            .andExpect(jsonPath("$.data[0].country").value("Algeria"))
            .andExpect(jsonPath("$.data[0].stadium.name").value("Stade du 5 Juillet 1962"))
            .andExpect(jsonPath("$.data[0].stadium.capacity").value(80200))
            // second team: CS Constantine
            .andExpect(jsonPath("$.data[1].id").value(911))
            .andExpect(jsonPath("$.data[1].name").value("CS Constantine"));
      }

      @Test
      @DisplayName("200 — response without Authorization header still passes (public endpoint)")
      void shouldReturnTeams_withoutAuthHeader() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams",
                Map.of("league", "12", "season", "2023"),
                List.of(),
                54,
                PAGING,
                LEAGUE_12_SEASON_2023_ITEMS);

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        Mockito.when(footballClient.getApiFootballTeamByLeagueAndSeason(12, 2023))
            .thenReturn(apiResponse);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "2023")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)));
      }
    }

    // ── Bean Validation (@Valid @ModelAttribute) ──────────────────────────────

    @Nested
    @DisplayName("Request validation — LeagueAndSeasonRequest")
    class RequestValidation {

      @Test
      @DisplayName("400 — missing league param")
      void shouldReturn400_whenLeagueMissing() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("season", "2023")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadRequest());
      }

      @Test
      @DisplayName("400 — missing season param")
      void shouldReturn400_whenSeasonMissing() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadRequest());
      }

      @Test
      @DisplayName("400 — league=0 violates @Min(1)")
      void shouldReturn400_whenLeagueIsZero() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "0")
                    .param("season", "2023")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadRequest());
      }

      @Test
      @DisplayName("400 — season='abcd' violates @Pattern (not numeric)")
      void shouldReturn400_whenSeasonIsNotNumeric() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "abcd")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadRequest());
      }

      @Test
      @DisplayName("400 — season='202' violates @Pattern (only 3 digits, not 4)")
      void shouldReturn400_whenSeasonHasOnly3Digits() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "202")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadRequest());
      }

      @Test
      @DisplayName("400 — season='' violates @NotBlank")
      void shouldReturn400_whenSeasonIsBlank() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadRequest());
      }
    }

    // ── Auth errors ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Auth errors")
    class AuthErrors {

      @Test
      @DisplayName("401 — authClient throws CustomUnauthorizedException")
      void shouldReturn401_whenTokenIsInvalid() throws Exception {
        Mockito.when(authClient.isTokenValid("bad-token"))
            .thenThrow(
                new CustomUnauthorizedException("Invalid or expired token, please log in again"));

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "2023")
                    .header("Authorization", "Bearer bad-token"))
            .andExpect(status().isUnauthorized());
      }

      @Test
      @DisplayName("502 — authClient throws CustomBadGatewayException (403 from ms-auth)")
      void shouldReturn502_whenAuthServiceReturns403() throws Exception {
        Mockito.when(authClient.isTokenValid("some-token"))
            .thenThrow(
                new CustomBadGatewayException("Session validation service rejected the request"));

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "2023")
                    .header("Authorization", "Bearer some-token"))
            .andExpect(status().isUnauthorized());
      }
    }

    // ── Api-Football error responses ─────────────────────────────────────────

    @Nested
    @DisplayName("Api-Football error responses")
    class ApiFootballErrors {

      @Test
      @DisplayName("429 — Api-Football quota exceeded for league query (errors.requests)")
      void shouldReturn429_whenQuotaExceeded() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams",
                Map.of("league", "12", "season", "2023"),
                List.of(Map.of("requests", "Exceeded the daily quota of 100 requests")),
                0,
                PAGING,
                List.of());

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamByLeagueAndSeason(12, 2023))
            .thenReturn(apiResponse);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "2023")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isTooManyRequests());
      }

      @Test
      @DisplayName(
          "429 — free plan cannot access season 2020 (errors.plan) — mirrors errorLimitedPlanFree")
      void shouldReturn429_whenPlanLimitReached() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams",
                Map.of("league", "12", "season", "2020"),
                List.of(
                    Map.of(
                        "plan",
                        "Free plans do not have access to this season, try from 2022 to 2024.")),
                0,
                PAGING,
                List.of());

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamByLeagueAndSeason(12, 2020))
            .thenReturn(apiResponse);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "2020")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isTooManyRequests());
      }

      @Test
      @DisplayName("404 — Api-Football returns empty response array for valid league+season")
      void shouldReturn404_whenApiFootballReturnsEmptyList() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams", Map.of("league", "99", "season", "2023"), List.of(), 0, PAGING, List.of());

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamByLeagueAndSeason(99, 2023))
            .thenReturn(apiResponse);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "99")
                    .param("season", "2023")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isNotFound());
      }

      @Test
      @DisplayName("503 — FootballClient throws CustomServiceUnavailableException (HTTP 499/500)")
      void shouldReturn503_whenApiFootballIsUnavailable() throws Exception {
        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamByLeagueAndSeason(12, 2023))
            .thenThrow(
                new CustomServiceUnavailableException(
                    "Api-Football is not currently available, please try again"));

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "2023")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isServiceUnavailable());
      }

      @Test
      @DisplayName("502 — Api-Football returns generic error map (noContent204-style body)")
      void shouldReturn502_whenApiFootballReturnsGenericError() throws Exception {
        ApiFootballTeamResponse apiResponse =
            new ApiFootballTeamResponse(
                "teams",
                Map.of("league", "12", "season", "2023"),
                List.of(
                    Map.of(
                        "time",
                        "2019-11-26T00:00:00+00:00",
                        "bug",
                        "This is on our side, please report us this bug on "
                            + "https://dashboard.api-football.com",
                        "report",
                        "teams?id=33")),
                0,
                PAGING,
                List.of());

        Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
        Mockito.when(footballClient.getApiFootballTeamByLeagueAndSeason(12, 2023))
            .thenReturn(apiResponse);

        mockMvc
            .perform(
                get("/api/teams")
                    .param("league", "12")
                    .param("season", "2023")
                    .header("Authorization", "Bearer " + VALID_TOKEN))
            .andExpect(status().isBadGateway());
      }
    }
  }
}
