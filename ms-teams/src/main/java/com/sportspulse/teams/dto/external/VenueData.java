package com.sportspulse.teams.dto.external;

/**
 * Represents venue information returned by the external API.
 *
 * @param name     venue name
 * @param city     city where the venue is located
 * @param capacity maximum seating capacity of the venue
 */
public record VenueData(String name, String city, Integer capacity) {}
