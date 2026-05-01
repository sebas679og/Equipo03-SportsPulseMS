package com.sportspulse.fixtures.dto.external;

/**
 * Data Transfer Object (DTO) representing a team within a fixture context.
 *
 * <p>This record provides identifying details for a team, including its visual representation and
 * the current goal count attributed specifically to this team object.
 *
 * @param id The unique identifier of the team.
 * @param name The display name of the team.
 * @param logo The URL pointing to the team's official logo image.
 * @param goals The number of goals scored by this specific team in the match.
 */
public record FootballFixtureTeam(Integer id, String name, String logo, Integer goals) {}
