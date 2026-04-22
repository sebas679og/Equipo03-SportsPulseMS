package com.sportspulse.leagues.dto.responses;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Detailed response for GET /api/leagues/{leagueId}. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueDetailResponse {

    private Integer id;
    private String name;
    private String type;
    private String country;
    private String logo;
    private List<Integer> seasons;
    private LeagueCurrentSeasonResponse currentSeason;
}
