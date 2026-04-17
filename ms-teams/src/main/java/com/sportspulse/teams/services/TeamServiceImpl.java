package com.sportspulse.teams.services;

import com.sportspulse.teams.dto.responses.StadiumResponse;
import com.sportspulse.teams.dto.responses.TeamResponse;
import com.sportspulse.teams.exceptions.CustomNotFoundException;
import com.sportspulse.teams.integration.football.FootballClient;
import com.sportspulse.teams.integration.football.dto.teamid.ApiFootballTeamResponse;
import com.sportspulse.teams.utils.mappers.TeamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * TeamServiceImpl Implementation of the {@link TeamService} interface that retrieves team data from
 * an external football API via {@link FootballClient}.
 *
 * <p>Maps the API response into domain-specific {@link TeamResponse} and {@link StadiumResponse}
 * objects, ensuring that the application works with consistent and structured data models.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

  private final FootballClient footballClient;
  private final TeamMapper teamMapper;

  @Override
  public TeamResponse getTeamById(int teamId) {

    ApiFootballTeamResponse api = footballClient.getApiFootballTeamById(teamId);

    var item =
        api.response().stream()
            .findFirst()
            .orElseThrow(
                () ->
                    new CustomNotFoundException(
                        "The requested team was not found in Api-Football"));

    return teamMapper.toTeamResponse(item);
  }
}
