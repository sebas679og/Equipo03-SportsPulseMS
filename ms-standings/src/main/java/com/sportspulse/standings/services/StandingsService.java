package com.sportspulse.standings.services;

import com.sportspulse.standings.dtos.request.LeagueAndSeasonRequest;
import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;
import com.sportspulse.standings.dtos.responses.TeamStandingLeagueAndSeasonResponse;

/**
 * StandingsService Service interface for retrieving league standings.
 *
 * <p>Defines the contract for accessing standings data by league and season, delegating the
 * implementation to service classes that interact with external APIs or data sources.
 */
public interface StandingsService {

  StandingsLeagueAndSeasonResponse getStandingsByLeagueAndSeason(LeagueAndSeasonRequest request);

  TeamStandingLeagueAndSeasonResponse getTeamStandingsByLeagueAndSeason(
      LeagueAndSeasonRequest request, int teamId);
}
