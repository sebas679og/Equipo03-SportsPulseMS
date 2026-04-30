package com.sportspulse.leagues.services;

import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeaguesResponse;

/** Leagues service contract. */
public interface LeaguesService {

  LeaguesResponse getLeagues(String country, String season);

  LeagueDetailResponse getLeagueById(int leagueId);
}
