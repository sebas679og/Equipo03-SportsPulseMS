package com.sportspulse.teams.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheConstants {
    public static final String TEAMS_CACHE = "teams";
    public static final String TEAMS_CACHE_LEAGUE_SEASON_KEY = "#league + '-' + #season";
}
