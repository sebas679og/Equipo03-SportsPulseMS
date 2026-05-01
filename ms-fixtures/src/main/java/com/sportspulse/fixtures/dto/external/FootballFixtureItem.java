package com.sportspulse.fixtures.dto.external;

/**
 * Data Transfer Object (DTO) representing a comprehensive summary of a football match.
 *
 * <p>This record serves as the primary data structure for a fixture entry, aggregating all key
 * components: schedule/venue details, league context, participating teams, and the scoreline.
 *
 * @param fixture The {@link FootballFixtureDetails} including ID, date, status, and venue.
 * @param league The {@link FootballFixtureLeague} associated with this match.
 * @param teams The {@link FootballFixtureTeams} identifying the home and away participants.
 * @param goals The {@link FootballFixtureGoals} tracking the current or final score.
 */
public record FootballFixtureItem(
    FootballFixtureDetails fixture,
    FootballFixtureLeague league,
    FootballFixtureTeams teams,
    FootballFixtureGoals goals) {}
