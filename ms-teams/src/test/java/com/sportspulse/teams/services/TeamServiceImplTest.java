package com.sportspulse.teams.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import com.sportspulse.teams.dto.requests.LeagueAndSeasonRequest;
import com.sportspulse.teams.dto.responses.DataLeagueSeasonResponse;
import com.sportspulse.teams.dto.responses.TeamByIdResponse;
import com.sportspulse.teams.dto.responses.TeamLeagueSeasonResponse;
import com.sportspulse.teams.exceptions.CustomBadGatewayException;
import com.sportspulse.teams.exceptions.CustomBadRequestException;
import com.sportspulse.teams.exceptions.CustomNotFoundException;
import com.sportspulse.teams.exceptions.CustomTooManyRequestsException;
import com.sportspulse.teams.integration.football.FootballClient;
import com.sportspulse.teams.integration.football.dto.teamid.ApiFootballTeamResponse;
import com.sportspulse.teams.integration.football.dto.teamid.ApiResponseItem;
import com.sportspulse.teams.utils.mappers.TeamMapper;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

  @Mock
  private FootballClient footballClient;

  @Mock
  private TeamMapper teamMapper;

  @InjectMocks
  private TeamServiceImpl teamService;

  private static final int TEAM_ID = 42;
  private static final int LEAGUE_ID = 10;
  private static final int SEASON = 2024;

  // -------------------------------------------------------------------------
  // Test helpers
  // -------------------------------------------------------------------------

  private ApiFootballTeamResponse buildApiResponseWithItems(List<ApiResponseItem> items) {
    ApiFootballTeamResponse response = mock(ApiFootballTeamResponse.class);
    given(response.response()).willReturn(items);
    given(response.errors()).willReturn(List.of());
    return response;
  }

  private ApiFootballTeamResponse buildApiResponseWithErrors(Map<String, Object> errorDetail) {
    ApiFootballTeamResponse response = mock(ApiFootballTeamResponse.class);
    given(response.errors()).willReturn(List.of(errorDetail));
    return response;
  }

  private LeagueAndSeasonRequest buildRequest(int league, int season) {
    LeagueAndSeasonRequest request = mock(LeagueAndSeasonRequest.class);
    given(request.getLeague()).willReturn(league);
    given(request.getSeason()).willReturn(String.valueOf(season));
    return request;
  }

  // =========================================================================
  // getTeamById()
  // =========================================================================

  // -------------------------------------------------------------------------
  // Input validation
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() throws CustomBadRequestException when teamId is zero")
  void getTeamById_whenTeamIdIsZero_throwsCustomBadRequestException() {
    assertThatThrownBy(() -> teamService.getTeamById(0))
            .isInstanceOf(CustomBadRequestException.class)
            .hasMessageContaining("positive number greater than 0");
  }

  @Test
  @DisplayName("getTeamById() throws CustomBadRequestException when teamId is negative")
  void getTeamById_whenTeamIdIsNegative_throwsCustomBadRequestException() {
    assertThatThrownBy(() -> teamService.getTeamById(-1))
            .isInstanceOf(CustomBadRequestException.class)
            .hasMessageContaining("positive number greater than 0");
  }

  @Test
  @DisplayName("getTeamById() does not call FootballClient when teamId is invalid")
  void getTeamById_whenTeamIdIsInvalid_doesNotCallFootballClient() {
    assertThatThrownBy(() -> teamService.getTeamById(0));

    then(footballClient).shouldHaveNoInteractions();
  }

  // -------------------------------------------------------------------------
  // Successful response
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() returns mapped TeamByIdResponse when API returns a result")
  void getTeamById_whenApiReturnsResult_returnsMappedResponse() {
    ApiResponseItem item = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of(item));
    TeamByIdResponse expected = mock(TeamByIdResponse.class);

    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);
    given(teamMapper.toTeamResponse(item)).willReturn(expected);

    TeamByIdResponse result = teamService.getTeamById(TEAM_ID);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("getTeamById() passes the first item from API response to the mapper")
  void getTeamById_passesFirstItemToMapper() {
    ApiResponseItem firstItem = mock(ApiResponseItem.class);
    ApiResponseItem secondItem = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of(firstItem, secondItem));

    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);
    given(teamMapper.toTeamResponse(firstItem)).willReturn(mock(TeamByIdResponse.class));

    teamService.getTeamById(TEAM_ID);

    then(teamMapper).should().toTeamResponse(firstItem);
    then(teamMapper).should(never()).toTeamResponse(secondItem);
  }

  // -------------------------------------------------------------------------
  // Empty response list → CustomNotFoundException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() throws CustomNotFoundException when API response list is empty")
  void getTeamById_whenResponseListIsEmpty_throwsCustomNotFoundException() {
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of());
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
            .isInstanceOf(CustomNotFoundException.class)
            .hasMessageContaining("not found in Api-Football");
  }

  @Test
  @DisplayName("getTeamById() does not call mapper when API response list is empty")
  void getTeamById_whenResponseListIsEmpty_doesNotCallMapper() {
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of());
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID));

    then(teamMapper).shouldHaveNoInteractions();
  }

  // -------------------------------------------------------------------------
  // handleApiFootballErrors() via getTeamById() — requests key → 429
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() throws CustomTooManyRequestsException when errors contain 'requests' key")
  void getTeamById_whenErrorsContainRequestsKey_throwsCustomTooManyRequestsException() {
    ApiFootballTeamResponse apiResponse = buildApiResponseWithErrors(
            Map.of("requests", "Rate limit exceeded"));
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
            .isInstanceOf(CustomTooManyRequestsException.class)
            .hasMessageContaining("daily request limit");
  }

  // -------------------------------------------------------------------------
  // handleApiFootballErrors() via getTeamById() — plan key → 429
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() throws CustomTooManyRequestsException when errors contain 'plan' key")
  void getTeamById_whenErrorsContainPlanKey_throwsCustomTooManyRequestsException() {
    ApiFootballTeamResponse apiResponse = buildApiResponseWithErrors(
            Map.of("plan", "Season limit exceeded"));
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
            .isInstanceOf(CustomTooManyRequestsException.class)
            .hasMessage("Season limit exceeded");
  }

  @Test
  @DisplayName("getTeamById() uses plan message as exception message when errors contain 'plan' key")
  void getTeamById_whenErrorsContainPlanKey_usesPlanMessageAsExceptionMessage() {
    String planMessage = "Your plan does not include this endpoint";
    ApiFootballTeamResponse apiResponse = buildApiResponseWithErrors(
            Map.of("plan", planMessage));
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
            .isInstanceOf(CustomTooManyRequestsException.class)
            .hasMessage(planMessage);
  }

  // -------------------------------------------------------------------------
  // handleApiFootballErrors() via getTeamById() — unknown key → CustomBadGatewayException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() throws CustomBadGatewayException when errors contain an unknown key")
  void getTeamById_whenErrorsContainUnknownKey_throwsCustomBadGatewayException() {
    ApiFootballTeamResponse apiResponse = buildApiResponseWithErrors(
            Map.of("token", "Invalid API token"));
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
            .isInstanceOf(CustomBadGatewayException.class)
            .hasMessageContaining("error occurred while processing");
  }

  // -------------------------------------------------------------------------
  // handleApiFootballErrors() via getTeamById() — null/empty errors → no exception
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() does not throw when errors list is null")
  void getTeamById_whenErrorsListIsNull_doesNotThrow() {
    ApiFootballTeamResponse apiResponse = mock(ApiFootballTeamResponse.class);
    ApiResponseItem item = mock(ApiResponseItem.class);
    given(apiResponse.errors()).willReturn(null);
    given(apiResponse.response()).willReturn(List.of(item));
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);
    given(teamMapper.toTeamResponse(item)).willReturn(mock(TeamByIdResponse.class));

    assertThatNoException().isThrownBy(() -> teamService.getTeamById(TEAM_ID));
  }

  @Test
  @DisplayName("getTeamById() does not throw when errors list is empty")
  void getTeamById_whenErrorsListIsEmpty_doesNotThrow() {
    ApiResponseItem item = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of(item));
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);
    given(teamMapper.toTeamResponse(item)).willReturn(mock(TeamByIdResponse.class));

    assertThatNoException().isThrownBy(() -> teamService.getTeamById(TEAM_ID));
  }

  // =========================================================================
  // getTeamLeagueSeasonByTeamId()
  // =========================================================================

  // -------------------------------------------------------------------------
  // Successful response
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamLeagueSeasonByTeamId() returns DataLeagueSeasonResponse with mapped items")
  void getTeamLeagueSeasonByTeamId_whenApiReturnsItems_returnsMappedResponse() {
    ApiResponseItem item = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of(item));
    LeagueAndSeasonRequest request = buildRequest(LEAGUE_ID, SEASON);

    given(footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON)).willReturn(apiResponse);
    given(teamMapper.toTeamLeagueSeasonResponse(item)).willReturn(mock(TeamLeagueSeasonResponse.class));

    DataLeagueSeasonResponse result = teamService.getTeamLeagueSeasonByTeamId(request);

    assertThat(result).isNotNull();
    assertThat((List<?>) getField(result, "data")).hasSize(1);
  }

  @Test
  @DisplayName("getTeamLeagueSeasonByTeamId() maps all items from API response")
  void getTeamLeagueSeasonByTeamId_mapsAllItemsFromApiResponse() {
    ApiResponseItem item1 = mock(ApiResponseItem.class);
    ApiResponseItem item2 = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of(item1, item2));
    LeagueAndSeasonRequest request = buildRequest(LEAGUE_ID, SEASON);

    given(footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON)).willReturn(apiResponse);
    given(teamMapper.toTeamLeagueSeasonResponse(any())).willReturn(mock(TeamLeagueSeasonResponse.class));

    DataLeagueSeasonResponse result = teamService.getTeamLeagueSeasonByTeamId(request);

    assertThat((List<?>) getField(result, "data")).hasSize(2);
    then(teamMapper).should(times(2)).toTeamLeagueSeasonResponse(any());
  }

  @Test
  @DisplayName("getTeamLeagueSeasonByTeamId() parses season string to int before calling FootballClient")
  void getTeamLeagueSeasonByTeamId_parsesSeasonStringToInt() {
    ApiResponseItem item = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of(item));
    LeagueAndSeasonRequest request = buildRequest(LEAGUE_ID, SEASON);

    given(footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON)).willReturn(apiResponse);
    given(teamMapper.toTeamLeagueSeasonResponse(item)).willReturn(mock(TeamLeagueSeasonResponse.class));

    teamService.getTeamLeagueSeasonByTeamId(request);

    then(footballClient).should().getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON);
  }

  // -------------------------------------------------------------------------
  // Empty/null response list → CustomNotFoundException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamLeagueSeasonByTeamId() throws CustomNotFoundException when response list is empty")
  void getTeamLeagueSeasonByTeamId_whenResponseListIsEmpty_throwsCustomNotFoundException() {
    ApiFootballTeamResponse apiResponse = buildApiResponseWithItems(List.of());
    LeagueAndSeasonRequest request = buildRequest(LEAGUE_ID, SEASON);
    given(footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamLeagueSeasonByTeamId(request))
            .isInstanceOf(CustomNotFoundException.class)
            .hasMessageContaining("league and season were not found");
  }

  @Test
  @DisplayName("getTeamLeagueSeasonByTeamId() throws CustomNotFoundException when response list is null")
  void getTeamLeagueSeasonByTeamId_whenResponseListIsNull_throwsCustomNotFoundException() {
    ApiFootballTeamResponse apiResponse = mock(ApiFootballTeamResponse.class);
    given(apiResponse.errors()).willReturn(List.of());
    given(apiResponse.response()).willReturn(null);
    LeagueAndSeasonRequest request = buildRequest(LEAGUE_ID, SEASON);
    given(footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamLeagueSeasonByTeamId(request))
            .isInstanceOf(CustomNotFoundException.class)
            .hasMessageContaining("league and season were not found");
  }

  // -------------------------------------------------------------------------
  // handleApiFootballErrors() via getTeamLeagueSeasonByTeamId()
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamLeagueSeasonByTeamId() throws CustomTooManyRequestsException when errors contain 'requests' key")
  void getTeamLeagueSeasonByTeamId_whenErrorsContainRequestsKey_throwsCustomTooManyRequestsException() {
    ApiFootballTeamResponse apiResponse = buildApiResponseWithErrors(
            Map.of("requests", "Rate limit exceeded"));
    LeagueAndSeasonRequest request = buildRequest(LEAGUE_ID, SEASON);
    given(footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamLeagueSeasonByTeamId(request))
            .isInstanceOf(CustomTooManyRequestsException.class)
            .hasMessageContaining("daily request limit");
  }

  @Test
  @DisplayName("getTeamLeagueSeasonByTeamId() throws CustomBadGatewayException when errors contain unknown key")
  void getTeamLeagueSeasonByTeamId_whenErrorsContainUnknownKey_throwsCustomBadGatewayException() {
    ApiFootballTeamResponse apiResponse = buildApiResponseWithErrors(
            Map.of("token", "Invalid token"));
    LeagueAndSeasonRequest request = buildRequest(LEAGUE_ID, SEASON);
    given(footballClient.getApiFootballTeamByLeagueAndSeason(LEAGUE_ID, SEASON)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamLeagueSeasonByTeamId(request))
            .isInstanceOf(CustomBadGatewayException.class)
            .hasMessageContaining("error occurred while processing");
  }

  // -------------------------------------------------------------------------
  // Season parsing error
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamLeagueSeasonByTeamId() throws NumberFormatException when season is not numeric")
  void getTeamLeagueSeasonByTeamId_whenSeasonIsNotNumeric_throwsNumberFormatException() {
    LeagueAndSeasonRequest request = mock(LeagueAndSeasonRequest.class);
    given(request.getSeason()).willReturn("invalid");

    assertThatThrownBy(() -> teamService.getTeamLeagueSeasonByTeamId(request))
            .isInstanceOf(NumberFormatException.class);
  }
}