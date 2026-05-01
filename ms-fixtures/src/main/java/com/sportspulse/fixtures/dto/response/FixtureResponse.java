package com.sportspulse.fixtures.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;

/**
 * Data Transfer Object (DTO) representing the primary detailed view of a football match.
 *
 * <p>This record serves as the central hub for match information, aggregating timing, competition
 * context, participating teams, and location data. It uses ISO-8601 formatting for the date to
 * ensure cross-platform compatibility.
 *
 * @param id The unique identifier of the fixture.
 * @param date The scheduled kickoff time, including offset for time zone awareness.
 * @param status The {@link StatusFixtureResponse} detailing the current state of the match.
 * @param league The {@link LeagueFixtureResponse} identifying the competition.
 * @param homeTeam The {@link TeamFixtureResponse} for the home side.
 * @param awayTeam The {@link TeamFixtureResponse} for the away side.
 * @param venue The {@link VenueFixtureResponse} identifying where the match is played.
 */
public record FixtureResponse(
    Long id,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime date,
    StatusFixtureResponse status,
    LeagueFixtureResponse league,
    TeamFixtureResponse homeTeam,
    TeamFixtureResponse awayTeam,
    VenueFixtureResponse venue) {}
