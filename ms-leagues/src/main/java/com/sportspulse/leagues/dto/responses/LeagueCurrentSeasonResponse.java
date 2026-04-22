package com.sportspulse.leagues.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Current season details in league detail response. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueCurrentSeasonResponse {

    private Integer year;
    private String startDate;
    private String endDate;
    private Boolean current;
}
