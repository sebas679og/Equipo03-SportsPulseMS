package com.sportspulse.fixtures.dto.external;

/**
 * Data Transfer Object (DTO) representing league-specific context for a fixture.
 *
 * <p>Beyond basic identification, this record includes the specific stage or round of the
 * competition, which is essential for differentiating between regular season weeks and tournament
 * phases.
 *
 * @param id The unique identifier of the league.
 * @param name The official name of the competition.
 * @param round The specific matchday or stage of the competition (e.g., "Regular Season - 12",
 *     "Quarter-finals").
 */
public record FootballFixtureLeague(Long id, String name, String round) {}
