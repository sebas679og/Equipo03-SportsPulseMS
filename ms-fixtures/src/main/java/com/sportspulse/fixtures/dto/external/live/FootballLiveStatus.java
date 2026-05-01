package com.sportspulse.fixtures.dto.external.live;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) representing the current temporal and operational state of an ongoing
 * match.
 *
 * <p>This record maps real-time match progress data, including abbreviated and descriptive status
 * labels, as well as the total minutes played.
 *
 * @param shortStatus The abbreviated code representing the match state (e.g., "1H", "HT", "2H").
 * @param longStatus The full descriptive name of the match state (e.g., "First Half", "Halftime").
 * @param elapsed The total number of minutes that have passed since the start of the match.
 */
public record FootballLiveStatus(
    @JsonProperty("short") String shortStatus,
    @JsonProperty("long") String longStatus,
    Integer elapsed) {}
