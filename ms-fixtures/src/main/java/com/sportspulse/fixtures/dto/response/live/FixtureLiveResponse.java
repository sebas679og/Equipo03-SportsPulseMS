package com.sportspulse.fixtures.dto.response.live;

/**
 * Data Transfer Object (DTO) representing the real-time state of an ongoing football match.
 *
 * <p>This record provides a high-level snapshot of a live fixture, combining temporal progress,
 * current match status, and team-specific scoring/details into a single unified response object.
 *
 * @param id The unique identifier of the fixture.
 * @param elapsed The current minute of the match.
 * @param status The {@link LiveStatusResponse} containing the current phase of the game.
 * @param league The {@link LiveLeagueResponse} providing competition context.
 * @param homeTeam The {@link LiveTeamResponse} for the hosting side.
 * @param awayTeam The {@link LiveTeamResponse} for the visiting side.
 */
public record FixtureLiveResponse(
    Long id,
    Integer elapsed,
    LiveStatusResponse status,
    LiveLeagueResponse league,
    LiveTeamResponse homeTeam,
    LiveTeamResponse awayTeam) {}
