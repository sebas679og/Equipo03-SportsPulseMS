package com.sportspulse.leagues.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ApiFixtures Data transfer object (DTO) representing fixture-related configuration flags returned
 * by the external football API.
 *
 * <p>Contains boolean indicators for whether events, lineups, fixture statistics, and player
 * statistics are included in the API response.
 */
public record ApiFixtures(
    boolean events,
    boolean lineups,
    @JsonProperty("statistics_fixtures") boolean statisticsFixtures,
    @JsonProperty("statistics_players") boolean statisticsPlayers) {}
