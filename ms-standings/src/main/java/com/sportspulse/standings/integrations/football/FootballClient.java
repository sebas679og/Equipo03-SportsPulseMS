package com.sportspulse.standings.integrations.football;

import com.sportspulse.standings.integrations.football.dto.StandingsResponse;

/**
 * FootballClient Interface representing a client for interacting with external football-related
 * APIs.
 *
 * <p>Serves as a contract for defining methods that retrieve football data such as teams, leagues,
 * and standings. Implementations of this interface are responsible for handling API communication
 * and data mapping.
 */
public interface FootballClient {

  StandingsResponse getStandingsForLeagueAndSeason(int leagueId, int season);
}
