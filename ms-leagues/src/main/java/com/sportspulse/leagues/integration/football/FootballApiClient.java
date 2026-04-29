package com.sportspulse.leagues.integration.football;

import com.sportspulse.leagues.integration.football.dto.ApiLeagueResponse;

/** Client abstraction for API-Football leagues endpoint. */
public interface FootballApiClient {

  ApiLeagueResponse getLeagues(String country, int season, int leagueId);
}
