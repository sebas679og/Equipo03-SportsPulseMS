package com.sportspulse.leagues;

import com.sportspulse.leagues.dto.responses.LeagueCurrentSeasonResponse;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummaryResponse;
import com.sportspulse.leagues.integration.dto.ApiFootballCountry;
import com.sportspulse.leagues.integration.dto.ApiFootballLeague;
import com.sportspulse.leagues.integration.dto.ApiFootballLeagueWrapper;
import com.sportspulse.leagues.integration.dto.ApiFootballSeason;
import java.util.List;

/** Shared test fixtures for ms-leagues tests. */
public final class LeaguesTestDataProvider {

  public static final Integer LEAGUE_ID = 140;
  public static final String LEAGUE_NAME = "La Liga";
  public static final String COUNTRY = "Spain";
  public static final Integer CURRENT_SEASON = 2024;

  private LeaguesTestDataProvider() {}

  public static ApiFootballLeagueWrapper apiLeagueWrapper() {
    ApiFootballLeague league =
        new ApiFootballLeague(LEAGUE_ID, LEAGUE_NAME, "League", "https://logo.test/140.png");
    ApiFootballCountry country = new ApiFootballCountry(COUNTRY);
    List<ApiFootballSeason> seasons =
        List.of(
            new ApiFootballSeason(2023, "2023-08-10", "2024-05-20", false),
            new ApiFootballSeason(2024, "2024-08-17", "2025-05-25", true));

    return new ApiFootballLeagueWrapper(league, country, seasons);
  }

  public static LeagueSummaryResponse summaryResponse() {
    return new LeagueSummaryResponse(
        LEAGUE_ID,
        LEAGUE_NAME,
        "League",
        COUNTRY,
        "https://logo.test/140.png",
        CURRENT_SEASON,
        "2024-08-17",
        "2025-05-25");
  }

  public static LeagueDetailResponse detailResponse() {
    return new LeagueDetailResponse(
        LEAGUE_ID,
        LEAGUE_NAME,
        "League",
        COUNTRY,
        "https://logo.test/140.png",
        List.of(2023, 2024),
        new LeagueCurrentSeasonResponse(2024, "2024-08-17", "2025-05-25", true));
  }
}
