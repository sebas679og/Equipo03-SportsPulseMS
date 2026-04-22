package com.sportspulse.leagues.integration.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** League wrapper object returned by API-Football. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiFootballLeagueWrapper {

    private ApiFootballLeague league;
    private ApiFootballCountry country;
    private List<ApiFootballSeason> seasons;
}
