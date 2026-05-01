package com.sportspulse.fixtures.dto.external;

/**
 * Data Transfer Object (DTO) representing the scoreline of a football match.
 *
 * <p>This record tracks the number of goals scored by each side. Depending on the API context,
 * these values may represent the current live score or the final result of the fixture.
 *
 * @param home The number of goals scored by the home team.
 * @param away The number of goals scored by the away team.
 */
public record FootballFixtureGoals(Integer home, Integer away) {}
