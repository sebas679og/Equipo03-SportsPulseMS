package com.sportspulse.leagues.integrations.football;

import com.sportspulse.leagues.integrations.football.dto.ApiLeagueResponse;

/** Client abstraction for API-Football leagues endpoint. */
public interface FootballApiClient {

  ApiLeagueResponse getLeaguesCountryAndSeason(String country, Integer season);

  ApiLeagueResponse getLeagueById(int leagueId);
}
