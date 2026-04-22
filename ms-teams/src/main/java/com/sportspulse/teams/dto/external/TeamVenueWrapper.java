package com.sportspulse.teams.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wraps team and venue information returned by the external API.
 *
 * <p>The external provider groups both objects under separate JSON fields ("team" and "venue"),
 * which are mapped to this record.
 *
 * @param team team information returned by the API
 * @param venue venue information associated with the team
 */
public record TeamVenueWrapper(
    @JsonProperty("team") TeamData team, @JsonProperty("venue") VenueData venue) {}
