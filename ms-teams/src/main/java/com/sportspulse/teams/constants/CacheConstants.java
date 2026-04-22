package com.sportspulse.teams.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Contains cache-related constants used across the teams module. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheConstants {
  public static final String TEAMS_CACHE = "teams";
  public static final String TEAMS_CACHE_LEAGUE_SEASON_KEY = "#league + '-' + #season";
}
