package com.sportspulse.standings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sportspulse.standings.dtos.request.LeagueAndSeasonRequest;
import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;
import com.sportspulse.standings.dtos.responses.TeamStandingLeagueAndSeasonResponse;
import com.sportspulse.standings.exceptions.CustomBadRequestException;
import com.sportspulse.standings.exceptions.CustomNotFoundException;
import com.sportspulse.standings.integrations.football.dto.ApiLeague;
import com.sportspulse.standings.integrations.football.dto.ApiResponse;
import com.sportspulse.standings.integrations.football.dto.ApiStanding;
import com.sportspulse.standings.integrations.football.dto.ApiStandingsResponse;
import com.sportspulse.standings.integrations.football.dto.ApiTeam;
import com.sportspulse.standings.services.StandingsServiceImpl;
import com.sportspulse.standings.services.complements.ApiFootballStandingsFetcher;
import com.sportspulse.standings.utils.mappers.StandingsMapper;
import com.sportspulse.standings.utils.mappers.TeamStandingMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StandingsServiceImpl")
class StandingsServiceImplTest {

  // ---------------------------------------------------------------------------
  // Shared constants
  // ---------------------------------------------------------------------------

  private static final int LEAGUE_ID = 39;
  private static final String SEASON_STR = "2023";
  private static final int SEASON_INT = 2023;
  private static final int TEAM_ID = 42;

  // ---------------------------------------------------------------------------
  // Mocks & subject under test
  // ---------------------------------------------------------------------------

  @Mock private ApiFootballStandingsFetcher apiFootballStandingsFetcher;
  @Mock private StandingsMapper standingsMapper;
  @Mock private TeamStandingMapper teamStandingMapper;

  @InjectMocks private StandingsServiceImpl service;

  // ---------------------------------------------------------------------------
  // Builders / helpers
  // ---------------------------------------------------------------------------

  /** Minimal request object for the given league + season. */
  private LeagueAndSeasonRequest request() {
    LeagueAndSeasonRequest req = new LeagueAndSeasonRequest();
    req.setLeague(StandingsServiceImplTest.LEAGUE_ID);
    req.setSeason(StandingsServiceImplTest.SEASON_STR);
    return req;
  }

  /** Default request using the class-level constants. */
  private LeagueAndSeasonRequest defaultRequest() {
    return request();
  }

  /**
   * Builds an {@link ApiStandingsResponse} whose first {@link ApiResponse} contains the supplied
   * {@link ApiLeague}.
   */
  private ApiStandingsResponse apiResponseFor(ApiLeague league) {
    ApiResponse apiResponse = new ApiResponse(league);
    return new ApiStandingsResponse("standings", null, List.of(), 1, null, List.of(apiResponse));
  }

  /**
   * Builds an {@link ApiLeague} with the given standings rows flattened into a single inner list.
   * Adjust the record constructor call to match your actual {@link ApiLeague} signature.
   */
  private ApiLeague leagueWithStandings(List<ApiStanding> standings) {
    // ApiLeague(id, name, country, logo, flag, season, standings)
    return new ApiLeague(
        LEAGUE_ID, "Premier League", "England", null, null, SEASON_INT, List.of(standings));
  }

  /** Creates an {@link ApiStanding} for the given team id. */
  private ApiStanding standingFor(int teamId) {
    ApiTeam team = new ApiTeam(teamId, "Team " + teamId, null);
    // Adjust constructor args to match your actual ApiStanding record fields
    return new ApiStanding(
        1, team, 10, 7, null, null, null, "Champions League", null, null, null, null);
  }

  // ===========================================================================
  // getStandingsByLeagueAndSeason
  // ===========================================================================

  @Nested
  @DisplayName("getStandingsByLeagueAndSeason")
  class GetStandingsByLeagueAndSeason {

    @Test
    @DisplayName("returns mapped response when API call succeeds")
    void returnsMappedResponse_whenApiCallSucceeds() {
      ApiLeague apiLeague = leagueWithStandings(List.of(standingFor(TEAM_ID)));
      ApiStandingsResponse apiStandingsResponse = apiResponseFor(apiLeague);
      StandingsLeagueAndSeasonResponse expected =
          StandingsLeagueAndSeasonResponse.builder().build();

      when(apiFootballStandingsFetcher.fetchValidatedStandings(LEAGUE_ID, SEASON_INT))
          .thenReturn(apiStandingsResponse);
      when(standingsMapper.toResponseFromLeague(apiLeague)).thenReturn(expected);

      StandingsLeagueAndSeasonResponse result =
          service.getStandingsByLeagueAndSeason(defaultRequest());

      assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("delegates to fetcher with league id and parsed season")
    void delegatesToFetcher_withParsedSeason() {
      ApiLeague apiLeague = leagueWithStandings(List.of(standingFor(TEAM_ID)));
      when(apiFootballStandingsFetcher.fetchValidatedStandings(LEAGUE_ID, SEASON_INT))
          .thenReturn(apiResponseFor(apiLeague));
      when(standingsMapper.toResponseFromLeague(apiLeague))
          .thenReturn(StandingsLeagueAndSeasonResponse.builder().build());

      service.getStandingsByLeagueAndSeason(defaultRequest());

      verify(apiFootballStandingsFetcher).fetchValidatedStandings(LEAGUE_ID, SEASON_INT);
    }

    @Test
    @DisplayName("passes the first response's league to the mapper")
    void passesFirstResponseLeague_toMapper() {
      ApiLeague apiLeague = leagueWithStandings(List.of(standingFor(TEAM_ID)));
      when(apiFootballStandingsFetcher.fetchValidatedStandings(LEAGUE_ID, SEASON_INT))
          .thenReturn(apiResponseFor(apiLeague));
      when(standingsMapper.toResponseFromLeague(apiLeague))
          .thenReturn(StandingsLeagueAndSeasonResponse.builder().build());

      service.getStandingsByLeagueAndSeason(defaultRequest());

      verify(standingsMapper).toResponseFromLeague(apiLeague);
    }
  }

  // ===========================================================================
  // getTeamStandingsByLeagueAndSeason
  // ===========================================================================

  @Nested
  @DisplayName("getTeamStandingsByLeagueAndSeason")
  class GetTeamStandingsByLeagueAndSeason {

    // -----------------------------------------------------------------------
    // teamId validation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("throws CustomBadRequestException when teamId is zero")
    void throwsBadRequest_whenTeamIdIsZero() {
      assertThatThrownBy(() -> service.getTeamStandingsByLeagueAndSeason(defaultRequest(), 0))
          .isInstanceOf(CustomBadRequestException.class)
          .hasMessageContaining("teamId must be a positive number greater than 0");

      verifyNoInteractions(apiFootballStandingsFetcher);
    }

    @Test
    @DisplayName("throws CustomBadRequestException when teamId is negative")
    void throwsBadRequest_whenTeamIdIsNegative() {
      assertThatThrownBy(() -> service.getTeamStandingsByLeagueAndSeason(defaultRequest(), -5))
          .isInstanceOf(CustomBadRequestException.class)
          .hasMessageContaining("teamId must be a positive number greater than 0");

      verifyNoInteractions(apiFootballStandingsFetcher);
    }

    // -----------------------------------------------------------------------
    // Happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("returns mapped response when team is found in standings")
    void returnsMappedResponse_whenTeamFoundInStandings() {
      ApiStanding standing = standingFor(TEAM_ID);
      ApiLeague apiLeague = leagueWithStandings(List.of(standing));
      TeamStandingLeagueAndSeasonResponse expected =
          TeamStandingLeagueAndSeasonResponse.builder().build();

      when(apiFootballStandingsFetcher.fetchValidatedStandings(LEAGUE_ID, SEASON_INT))
          .thenReturn(apiResponseFor(apiLeague));
      when(teamStandingMapper.toResponse(standing, apiLeague)).thenReturn(expected);

      TeamStandingLeagueAndSeasonResponse result =
          service.getTeamStandingsByLeagueAndSeason(defaultRequest(), TEAM_ID);

      assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("finds the correct team when standings contain multiple teams")
    void findsCorrectTeam_whenMultipleTeamsInStandings() {
      ApiStanding targetStanding = standingFor(TEAM_ID);
      ApiLeague apiLeague =
          leagueWithStandings(List.of(standingFor(10), standingFor(20), targetStanding));
      TeamStandingLeagueAndSeasonResponse expected =
          TeamStandingLeagueAndSeasonResponse.builder().build();

      when(apiFootballStandingsFetcher.fetchValidatedStandings(LEAGUE_ID, SEASON_INT))
          .thenReturn(apiResponseFor(apiLeague));
      when(teamStandingMapper.toResponse(targetStanding, apiLeague)).thenReturn(expected);

      TeamStandingLeagueAndSeasonResponse result =
          service.getTeamStandingsByLeagueAndSeason(defaultRequest(), TEAM_ID);

      assertThat(result).isEqualTo(expected);
      verify(teamStandingMapper).toResponse(targetStanding, apiLeague);
    }

    @Test
    @DisplayName("passes the fetched league to the team standing mapper")
    void passesLeague_toTeamStandingMapper() {
      ApiStanding standing = standingFor(TEAM_ID);
      ApiLeague apiLeague = leagueWithStandings(List.of(standing));

      when(apiFootballStandingsFetcher.fetchValidatedStandings(LEAGUE_ID, SEASON_INT))
          .thenReturn(apiResponseFor(apiLeague));
      when(teamStandingMapper.toResponse(standing, apiLeague))
          .thenReturn(TeamStandingLeagueAndSeasonResponse.builder().build());

      service.getTeamStandingsByLeagueAndSeason(defaultRequest(), TEAM_ID);

      verify(teamStandingMapper).toResponse(standing, apiLeague);
    }

    // -----------------------------------------------------------------------
    // Team not found
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("throws CustomNotFoundException when teamId is not present in standings")
    void throwsNotFoundException_whenTeamNotInStandings() {
      ApiLeague apiLeague = leagueWithStandings(List.of(standingFor(99)));

      when(apiFootballStandingsFetcher.fetchValidatedStandings(LEAGUE_ID, SEASON_INT))
          .thenReturn(apiResponseFor(apiLeague));

      assertThatThrownBy(() -> service.getTeamStandingsByLeagueAndSeason(defaultRequest(), TEAM_ID))
          .isInstanceOf(CustomNotFoundException.class)
          .hasMessageContaining(String.valueOf(TEAM_ID))
          .hasMessageContaining(String.valueOf(LEAGUE_ID))
          .hasMessageContaining(SEASON_STR);
    }

    @Test
    @DisplayName("throws CustomNotFoundException when standings list is empty")
    void throwsNotFoundException_whenStandingsListIsEmpty() {
      ApiLeague apiLeague = leagueWithStandings(List.of());

      when(apiFootballStandingsFetcher.fetchValidatedStandings(LEAGUE_ID, SEASON_INT))
          .thenReturn(apiResponseFor(apiLeague));

      assertThatThrownBy(() -> service.getTeamStandingsByLeagueAndSeason(defaultRequest(), TEAM_ID))
          .isInstanceOf(CustomNotFoundException.class)
          .hasMessageContaining(String.valueOf(TEAM_ID));
    }
  }
}
