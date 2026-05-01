package com.sportspulse.fixtures.dto.external.live;

/**
 * Data Transfer Object (DTO) representing the league information for a live match.
 *
 * <p>This record provides the basic identification and name of the football competition associated
 * with an ongoing fixture as reported by the live data provider.
 *
 * @param id The unique identifier of the league.
 * @param name The official name of the competition (e.g., "Premier League", "La Liga").
 */
public record FootballLiveLeague(Long id, String name) {}
