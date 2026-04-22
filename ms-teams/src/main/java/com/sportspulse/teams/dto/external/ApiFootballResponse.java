package com.sportspulse.teams.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ApiFootballResponse(
        @JsonProperty("response")
        List<TeamVenueWrapper> teams
) {
}
