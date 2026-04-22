package com.sportspulse.leagues.services;

import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummaryResponse;
import java.util.List;

/** Leagues service contract. */
public interface LeaguesService {

  List<LeagueSummaryResponse> getLeagues(String country, Integer season);

  LeagueDetailResponse getLeagueById(Integer leagueId);
}
