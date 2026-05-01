package com.sportspulse.fixtures.dto.external;

/**
 * Data Transfer Object (DTO) representing the location where a match is held.
 *
 * <p>This record provides the geographical and physical context for the fixture, identifying both
 * the specific stadium and the city hosting the event.
 *
 * @param name The official name of the stadium or venue (e.g., "Old Trafford", "Camp Nou").
 * @param city The name of the city where the venue is located.
 */
public record FootballFixtureVenue(String name, String city) {}
