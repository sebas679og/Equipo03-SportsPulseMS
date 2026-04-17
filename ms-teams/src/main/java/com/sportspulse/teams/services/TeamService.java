package com.sportspulse.teams.services;

import com.sportspulse.teams.dto.responses.TeamResponse;

/**
 * TeamService Service interface that defines operations related to teams. Provides methods to
 * retrieve team information by unique identifiers.
 */
public interface TeamService {

  TeamResponse getTeamById(int teamId);
}
