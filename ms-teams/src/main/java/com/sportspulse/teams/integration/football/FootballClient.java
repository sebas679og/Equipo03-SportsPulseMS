package com.sportspulse.teams.integration.football;

import com.sportspulse.teams.integration.football.dto.teamid.ApiFootballTeamResponse;

/**
 * FootballClient Defines the contract for interacting with the Football API. Provides methods to
 * retrieve football team information from the external service.
 */
public interface FootballClient {

  /**
   * Retrieves football team details from the Football API by team identifier.
   *
   * @param teamId the unique identifier of the team
   * @return an {@link ApiFootballTeamResponse} containing the team details
   */
  ApiFootballTeamResponse getApiFootballTeamById(int teamId);

  /**
   * Retrieves team information from the external football API based on a specific league and
   * season.
   *
   * @param leagueId the unique identifier of the league
   * @param season the season year to query
   * @return an {@link ApiFootballTeamResponse} containing the teams' details
   */
  ApiFootballTeamResponse getApiFootballTeamByLeagueAndSeason(int leagueId, int season);
}
