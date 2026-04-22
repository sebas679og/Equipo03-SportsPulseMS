package com.sportspulse.leagues.integration.client;

import com.sportspulse.leagues.integration.dto.ApiFootballLeagueWrapper;
import java.util.List;

/** Client abstraction for API-Football leagues endpoint. */
public interface FootballApiClient {

  List<ApiFootballLeagueWrapper> getLeagues(String country, Integer season, Integer leagueId);
}
