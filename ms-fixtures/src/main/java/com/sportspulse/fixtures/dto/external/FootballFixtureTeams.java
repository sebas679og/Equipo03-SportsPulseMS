package com.sportspulse.fixtures.dto.external;

/**
 * Data Transfer Object (DTO) grouping the two competing sides of a fixture.
 *
 * <p>This record provides a structured pair for the home and away teams, allowing for clear
 * distinction between the two participants and their respective match data.
 *
 * @param home The {@link FootballFixtureTeam} representing the hosting or "home" side.
 * @param away The {@link FootballFixtureTeam} representing the visiting or "away" side.
 */
public record FootballFixtureTeams(FootballFixtureTeam home, FootballFixtureTeam away) {}
