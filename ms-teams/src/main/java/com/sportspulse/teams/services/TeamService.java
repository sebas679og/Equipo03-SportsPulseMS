package com.sportspulse.teams.services;

import com.sportspulse.teams.dto.requests.LeagueAndSeasonRequest;
import com.sportspulse.teams.dto.responses.DataLeagueSeasonResponse;
import com.sportspulse.teams.dto.responses.TeamByIdResponse;

/**
 * TeamService Service interface that defines operations related to teams. Provides methods to
 * retrieve team information by unique identifiers.
 */
public interface TeamService {

  TeamByIdResponse getTeamById(int teamId);

  DataLeagueSeasonResponse getTeamLeagueSeasonByTeamId(LeagueAndSeasonRequest request);
}
