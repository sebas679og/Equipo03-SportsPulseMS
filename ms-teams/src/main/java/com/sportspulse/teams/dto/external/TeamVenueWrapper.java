package com.sportspulse.teams.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TeamVenueWrapper(
        @JsonProperty("team")
        TeamData team,

        @JsonProperty("venue")
        VenueData venue
) {
}
