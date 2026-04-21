package com.sportspulse.teams.services;

import com.sportspulse.teams.dto.requests.LeagueAndSeasonRequest;
import com.sportspulse.teams.dto.responses.DataLeagueSeasonResponse;
import com.sportspulse.teams.dto.responses.StadiumByIdResponse;
import com.sportspulse.teams.dto.responses.TeamByIdResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * TeamServiceImpl Implementation of the {@link TeamService} interface that retrieves team data from
 * an external football API via {@link FootballClient}.
 *
 * <p>Maps the API response into domain-specific {@link TeamByIdResponse} and {@link
 * StadiumByIdResponse} objects, ensuring that the application works with consistent and structured
 * data models.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

  private final FootballClient footballClient;
  private final TeamMapper teamMapper;

  @Override
  public TeamByIdResponse getTeamById(int teamId) {
    if (teamId <= 0) {
      throw new CustomBadRequestException("teamId must be a positive number greater than 0.");
    }

    ApiFootballTeamResponse api = footballClient.getApiFootballTeamById(teamId);

    handleApiFootballErrors(api);

    ApiResponseItem item =
        api.response().stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new CustomNotFoundException(
                        "The requested team was not found in Api-Football"));

    return teamMapper.toTeamResponse(item);
  }

  @Override
  public DataLeagueSeasonResponse getTeamLeagueSeasonByTeamId(LeagueAndSeasonRequest request) {
    int seasonInt = Integer.parseInt(request.getSeason());

    ApiFootballTeamResponse api =
        footballClient.getApiFootballTeamByLeagueAndSeason(request.getLeague(), seasonInt);
    handleApiFootballErrors(api);

    List<ApiResponseItem> items = api.response();

    if (items == null || items.isEmpty()) {
      throw new CustomNotFoundException(
          "The requested league and season were not found in Api-Football.");
    }

    return DataLeagueSeasonResponse.builder()
        .data(items.stream().map(teamMapper::toTeamLeagueSeasonResponse).toList())
        .build();
  }

  private void handleApiFootballErrors(ApiFootballTeamResponse api) {
    if (api.errors() == null || api.errors().isEmpty()) {
      return;
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> errorDetail = (Map<String, Object>) api.errors().getFirst();

    if (errorDetail.containsKey("requests")) {
      log.warn(
          "Api-Football 429 Rate Limit exceeded. "
              + "Available requests have been exhausted. Body: {}",
          errorDetail);
      throw new CustomTooManyRequestsException(
          "The daily request limit to Api-Football has been "
              + "reached, please try again tomorrow");
    } else if (errorDetail.containsKey("plan")) {
      String planMessage = (String) errorDetail.get("plan");
      log.warn(
          "Api-Football, the request limit per season has been exceeded. "
              + "Available requests have been exhausted. Body: {}",
          planMessage);
      throw new CustomTooManyRequestsException(planMessage);
    } else {
      log.error("Api-Football returned errors in the response. Body: {}", errorDetail);
      throw new CustomBadGatewayException(
          "An error occurred while processing the request to Api-Football. "
              + "Please try again later.");
    }
  }
}
