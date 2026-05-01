package com.sportspulse.fixtures.dto.response;

/**
 * Data Transfer Object (DTO) providing the location details for a standard fixture.
 *
 * <p>This record identifies where a match is scheduled to take place, offering essential geographic
 * context for fans and administrators.
 *
 * @param name The official name of the stadium or venue.
 * @param city The city where the venue is located.
 */
public record VenueFixtureResponse(String name, String city) {}
