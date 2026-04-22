package com.sportspulse.teams.dto.response;

import lombok.Builder;

/**
 * Represents venue information included in a team response.
 *
 * @param name     venue name
 * @param city     city where the venue is located
 * @param capacity maximum seating capacity of the venue
 */
@Builder
public record VenueResponse(String name, String city, Integer capacity) {}
