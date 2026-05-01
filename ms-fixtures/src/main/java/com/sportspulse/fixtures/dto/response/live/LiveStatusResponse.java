package com.sportspulse.fixtures.dto.response.live;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sportspulse.fixtures.enums.FixtureLiveStatus;

/**
 * Data Transfer Object (DTO) capturing the current phase and description of a live match.
 *
 * <p>This record uses an enum-based short status for programmatic logic (such as triggering UI
 * state changes) and a descriptive string for user-facing displays.
 *
 * @param status The {@link FixtureLiveStatus} enum representing the coded match state (e.g., 1H,
 *     HT, 2H).
 * @param description The full text description of the current status (e.g., "First Half", "Half
 *     Time").
 */
public record LiveStatusResponse(
    @JsonProperty("short") FixtureLiveStatus status, @JsonProperty("long") String description) {}
