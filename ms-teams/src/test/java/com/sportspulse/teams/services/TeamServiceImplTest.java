package com.sportspulse.teams.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.sportspulse.teams.dto.responses.TeamResponse;
import com.sportspulse.teams.exceptions.CustomNotFoundException;
import com.sportspulse.teams.exceptions.CustomServiceUnavailableException;
import com.sportspulse.teams.exceptions.CustomTooManyRequestsException;
import com.sportspulse.teams.integration.football.FootballClient;
import com.sportspulse.teams.integration.football.dto.teamid.ApiFootballTeamResponse;
import com.sportspulse.teams.integration.football.dto.teamid.ApiResponseItem;
import com.sportspulse.teams.utils.mappers.TeamMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

  @Mock private FootballClient footballClient;

  @Mock private TeamMapper teamMapper;

  @InjectMocks private TeamServiceImpl teamService;

  private static final int TEAM_ID = 42;

  // -------------------------------------------------------------------------
  // Test helpers
  // -------------------------------------------------------------------------

  private ApiFootballTeamResponse buildApiResponse(List<?> items) {
    ApiFootballTeamResponse response = mock(ApiFootballTeamResponse.class);
    given(response.response()).willReturn((List) items);
    return response;
  }

  // -------------------------------------------------------------------------
  // Successful response
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() returns the mapped TeamResponse when API returns a result")
  void getTeamById_whenApiReturnsResult_returnsMappedTeamResponse() {
    ApiResponseItem item = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponse(List.of(item));
    TeamResponse expected = mock(TeamResponse.class);

    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);
    given(teamMapper.toTeamResponse(item)).willReturn(expected);

    TeamResponse result = teamService.getTeamById(TEAM_ID);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("getTeamById() calls FootballClient with the correct team ID")
  void getTeamById_callsFootballClientWithCorrectTeamId() {
    ApiResponseItem item = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponse(List.of(item));

    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);
    given(teamMapper.toTeamResponse(any())).willReturn(mock(TeamResponse.class));

    teamService.getTeamById(TEAM_ID);

    then(footballClient).should().getApiFootballTeamById(TEAM_ID);
  }

  @Test
  @DisplayName("getTeamById() passes the first item from the API response to the mapper")
  void getTeamById_passesFirstItemToMapper() {
    ApiResponseItem firstItem = mock(ApiResponseItem.class);
    ApiResponseItem secondItem = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponse(List.of(firstItem, secondItem));

    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);
    given(teamMapper.toTeamResponse(firstItem)).willReturn(mock(TeamResponse.class));

    teamService.getTeamById(TEAM_ID);

    then(teamMapper).should().toTeamResponse(firstItem);
    then(teamMapper).should(never()).toTeamResponse(secondItem);
  }

  @Test
  @DisplayName("getTeamById() calls the mapper exactly once")
  void getTeamById_callsMapperExactlyOnce() {
    ApiResponseItem item = mock(ApiResponseItem.class);
    ApiFootballTeamResponse apiResponse = buildApiResponse(List.of(item));

    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);
    given(teamMapper.toTeamResponse(item)).willReturn(mock(TeamResponse.class));

    teamService.getTeamById(TEAM_ID);

    then(teamMapper).should(times(1)).toTeamResponse(any());
  }

  // -------------------------------------------------------------------------
  // Empty response list → CustomNotFoundException
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() throws CustomNotFoundException when API response list is empty")
  void getTeamById_whenApiResponseListIsEmpty_throwsCustomNotFoundException() {
    ApiFootballTeamResponse apiResponse = buildApiResponse(List.of());
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
        .isInstanceOf(CustomNotFoundException.class)
        .hasMessageContaining("not found in Api-Football");
  }

  @Test
  @DisplayName("getTeamById() does not call mapper when API response list is empty")
  void getTeamById_whenApiResponseListIsEmpty_doesNotCallMapper() {
    ApiFootballTeamResponse apiResponse = buildApiResponse(List.of());
    given(footballClient.getApiFootballTeamById(TEAM_ID)).willReturn(apiResponse);

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID));

    then(teamMapper).shouldHaveNoInteractions();
  }

  // -------------------------------------------------------------------------
  // FootballClient throws — exception propagates
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("getTeamById() propagates CustomNotFoundException thrown by FootballClient")
  void getTeamById_whenFootballClientThrowsNotFoundException_propagatesException() {
    given(footballClient.getApiFootballTeamById(TEAM_ID))
        .willThrow(new CustomNotFoundException("not found in Api-Football"));

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
        .isInstanceOf(CustomNotFoundException.class)
        .hasMessageContaining("not found in Api-Football");
  }

  @Test
  @DisplayName("getTeamById() propagates CustomTooManyRequestsException thrown by FootballClient")
  void getTeamById_whenFootballClientThrowsTooManyRequests_propagatesException() {
    given(footballClient.getApiFootballTeamById(TEAM_ID))
        .willThrow(new CustomTooManyRequestsException("rate limit reached"));

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
        .isInstanceOf(CustomTooManyRequestsException.class)
        .hasMessageContaining("rate limit reached");
  }

  @Test
  @DisplayName(
      "getTeamById() propagates CustomServiceUnavailableException thrown by FootballClient")
  void getTeamById_whenFootballClientThrowsServiceUnavailable_propagatesException() {
    given(footballClient.getApiFootballTeamById(TEAM_ID))
        .willThrow(new CustomServiceUnavailableException("service unavailable"));

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID))
        .isInstanceOf(CustomServiceUnavailableException.class)
        .hasMessageContaining("service unavailable");
  }

  @Test
  @DisplayName("getTeamById() does not call mapper when FootballClient throws")
  void getTeamById_whenFootballClientThrows_doesNotCallMapper() {
    given(footballClient.getApiFootballTeamById(TEAM_ID))
        .willThrow(new CustomServiceUnavailableException("service unavailable"));

    assertThatThrownBy(() -> teamService.getTeamById(TEAM_ID));

    then(teamMapper).shouldHaveNoInteractions();
  }
}
