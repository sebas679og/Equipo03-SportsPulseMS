package com.sportspulse.standings.services;

import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;

/**
 * StandingsService Service interface for retrieving league standings.
 *
 * <p>Defines the contract for accessing standings data by league and season, delegating the
 * implementation to service classes that interact with external APIs or data sources.
 */
public interface StandingsService {

  StandingsLeagueAndSeasonResponse getStandingsByLeagueAndSeason(int league, int season);
}
