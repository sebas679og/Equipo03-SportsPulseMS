package com.sportspulse.leagues.services;

import com.sportspulse.leagues.dto.requests.CountryAndSeasonRequest;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeaguesResponse;

/** Leagues service contract. */
public interface LeaguesService {

  LeaguesResponse getLeagues(CountryAndSeasonRequest request);

  LeagueDetailResponse getLeagueById(int leagueId);
}
