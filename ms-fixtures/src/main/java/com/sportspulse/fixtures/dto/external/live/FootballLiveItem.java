package com.sportspulse.fixtures.dto.external.live;

/**
 * Data Transfer Object (DTO) representing a single entry in a live match update.
 *
 * <p>This record acts as a comprehensive container for all real-time data of a specific match,
 * including the fixture's current status, the league it belongs to, and the competing teams.
 *
 * @param fixture The {@link FootballLiveFixture} details, including ID and live status.
 * @param league The {@link FootballLiveLeague} information for the match.
 * @param teams The {@link FootballLiveTeams} object containing data for home and away sides.
 */
public record FootballLiveItem(
    FootballLiveFixture fixture, FootballLiveLeague league, FootballLiveTeams teams) {}
