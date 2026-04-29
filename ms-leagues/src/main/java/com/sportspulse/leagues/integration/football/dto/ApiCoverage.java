package com.sportspulse.leagues.integration.football.dto;

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
    boolean top_scorers,
    boolean top_assists,
    boolean top_cards,
    boolean injuries,
    boolean predictions,
    boolean odds) {}
