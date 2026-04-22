package com.sportspulse.leagues.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** List item response for GET /api/leagues. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueSummaryResponse {

    private Integer id;
    private String name;
    private String type;
    private String country;
    private String logo;
    private Integer currentSeason;
    private String startDate;
    private String endDate;
}
