package com.sportspulse.fixtures.dto.external.live;

/**
 * Data Transfer Object (DTO) representing the two opposing teams in a live match.
 *
 * <p>This record groups the home and away sides together, providing a structured way to access
 * team-specific data for an ongoing fixture.
 *
 * @param home The {@link FootballLiveTeam} representing the home side.
 * @param away The {@link FootballLiveTeam} representing the away side.
 */
public record FootballLiveTeams(FootballLiveTeam home, FootballLiveTeam away) {}
