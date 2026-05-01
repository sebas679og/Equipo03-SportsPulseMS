package com.sportspulse.fixtures.dto.external;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object (DTO) containing the core details of a football fixture.
 *
 * <p>This record encapsulates the fundamental identifying and situational information for a match,
 * including its unique ID, scheduled timing, current operational status, and the venue where it is
 * being held.
 *
 * @param id The unique identifier for the fixture.
 * @param date The scheduled date and time of the match in ISO 8601 format with offset.
 * @param status The {@link FootballFixtureStatus} representing the match's current state.
 * @param venue The {@link FootballFixtureVenue} where the match takes place.
 */
public record FootballFixtureDetails(
    Integer id, OffsetDateTime date, FootballFixtureStatus status, FootballFixtureVenue venue) {}
