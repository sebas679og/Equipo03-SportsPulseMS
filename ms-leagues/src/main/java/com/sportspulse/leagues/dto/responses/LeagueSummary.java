package com.sportspulse.leagues.dto.responses;

import lombok.Builder;
import lombok.Value;

/** List item response for GET /api/leagues. */
@Value
@Builder
public class LeagueSummary {
  int id;
  String name;
  String type;
  String country;
  String logo;
  int currentSeason;
  String startDate;
  String endDate;
}
