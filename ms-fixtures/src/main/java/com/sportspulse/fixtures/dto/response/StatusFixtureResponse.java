package com.sportspulse.fixtures.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sportspulse.fixtures.enums.FixtureStatus;

/**
 * Data Transfer Object (DTO) capturing the lifecycle state of a standard fixture.
 *
 * <p>This record maps the match status to both a standardized code (short) and a human-readable
 * description (long). It is typically used in general fixture listings to indicate if a match is
 * "Not Started", "Finished", or "Postponed".
 *
 * @param status The {@link FixtureStatus} enum representing the status code (e.g., NS, FT, PST).
 * @param description The full text description of the match state (e.g., "Match Finished").
 */
public record StatusFixtureResponse(
    @JsonProperty("short") FixtureStatus status, @JsonProperty("long") String description) {}
