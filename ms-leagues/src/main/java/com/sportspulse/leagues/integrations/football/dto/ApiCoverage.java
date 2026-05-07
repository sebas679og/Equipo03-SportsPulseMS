package com.sportspulse.leagues.integrations.football.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ApiCoverage Data transfer object (DTO) representing coverage details provided by the external
 * football API.
 *
 * <p>Encapsulates configuration flags for fixtures, standings, player data, top scorers, assists,
 * disciplinary cards, injuries, predictions, and betting odds.
 *
 * <p>Includes a nested {@link ApiFixtures} object to represent fixture-specific coverage options.
 */
public record ApiCoverage(
    ApiFixtures fixtures,
    boolean standings,
    boolean players,
    @JsonProperty("top_scorers") boolean topScorers,
    @JsonProperty("top_assists") boolean topAssists,
    @JsonProperty("top_cards") boolean topCards,
    boolean injuries,
    boolean predictions,
    boolean odds) {}
