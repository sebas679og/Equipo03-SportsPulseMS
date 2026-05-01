package com.sportspulse.fixtures.dto.response;

/**
 * Data Transfer Object (DTO) providing specific competition context for a fixture.
 *
 * <p>Unlike the lightweight live version, this record includes the competition round, which is
 * essential for identifying the specific stage of the tournament (e.g., "Regular Season - 22",
 * "Quarter-finals").
 *
 * @param id The unique identifier of the league.
 * @param name The official name of the league or tournament.
 * @param round The specific round or phase of the competition.
 */
public record LeagueFixtureResponse(Long id, String name, String round) {}
