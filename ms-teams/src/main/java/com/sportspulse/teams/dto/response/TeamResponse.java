package com.sportspulse.teams.dto.response;

import lombok.Builder;

/**
 * Represents the API response containing team information.
 *
 * @param id unique identifier of the team
 * @param name official team name
 * @param country country where the team is based
 * @param logo URL of the team's logo
 * @param founded year the team was founded
 * @param venue venue information associated with the team
 */
@Builder
public record TeamResponse(
    Integer id, String name, String country, String logo, Integer founded, VenueResponse venue) {}
