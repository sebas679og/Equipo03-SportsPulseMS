package com.sportspulse.fixtures.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiScore(
        ApiScoreDetail halftime,
        ApiScoreDetail fulltime,
        ApiScoreDetail extratime,
        ApiScoreDetail penalty) {
}
