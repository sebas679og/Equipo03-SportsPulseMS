package com.sportspulse.fixtures.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) representing the current status and timing of a fixture.
 *
 * <p>This record maps the temporal state of a match, providing both technical codes for logic
 * processing and human-readable strings for display, alongside the current match clock.
 *
 * @param shortStatus The abbreviated status code (e.g., "NS" for Not Started, "FT" for Full Time).
 * @param longStatus The full descriptive status of the match (e.g., "Match Finished").
 * @param elapsed The number of minutes elapsed in the match, if currently in progress.
 */
public record FootballFixtureStatus(
    @JsonProperty("short") String shortStatus,
    @JsonProperty("long") String longStatus,
    Integer elapsed) {}
