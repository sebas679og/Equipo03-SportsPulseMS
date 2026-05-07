package com.sportspulse.leagues.dto.responses;

import lombok.Builder;
import lombok.Value;

/** Current season details in league detail response. */
@Value
@Builder
public class LeagueCurrentSeason {
  Integer year;
  String startDate;
  String endDate;
  Boolean current;
}
