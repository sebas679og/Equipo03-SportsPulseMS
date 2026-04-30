package com.sportspulse.leagues.dto.responses;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LeaguesResponse {
  List<LeagueSummary> data;
}
