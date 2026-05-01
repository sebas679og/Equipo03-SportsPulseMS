package com.sportspulse.fixtures.dto.external.live;

/**
 * Data Transfer Object (DTO) representing a team participating in a live match.
 *
 * <p>This record encapsulates the basic identity details of a team, typically used within a live
 * tracking context to distinguish between the home and away sides.
 *
 * @param id The unique identifier of the team.
 * @param name The display name of the team.
 */
public record FootballLiveTeam(Long id, String name) {}
