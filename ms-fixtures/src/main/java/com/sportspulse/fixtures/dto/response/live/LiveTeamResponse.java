package com.sportspulse.fixtures.dto.response.live;

/**
 * Data Transfer Object (DTO) capturing the essential scoreline data for a team during a live match.
 *
 * <p>This record provides a lightweight snapshot of a team's current performance, focusing strictly
 * on identity and the primary "live" metric: the current goal count.
 *
 * @param id The unique identifier of the team.
 * @param name The display name of the team.
 * @param goals The current number of goals scored by this team.
 */
public record LiveTeamResponse(Long id, String name, Integer goals) {}
