package com.sportspulse.leagues.dto.responses;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/** Detailed response for GET /api/leagues/{leagueId}. */
@Value
@Builder
public class LeagueDetailResponse {
  int id;
  String name;
  String type;
  String country;
  String logo;
  List<Integer> seasons;
  LeagueCurrentSeason currentSeason;
}
