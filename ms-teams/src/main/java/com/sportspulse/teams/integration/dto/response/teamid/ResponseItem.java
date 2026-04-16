package com.sportspulse.teams.integration.dto.response.teamid;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/**
 * ResponseItem
 * Represents a composite response containing both team and venue details.
 * Serves as a wrapper object to group related entities in service responses.
 */
@Value
@Getter
@Builder
public class ResponseItem {
    TeamResponse team;
    VenueResponse venue;
}

