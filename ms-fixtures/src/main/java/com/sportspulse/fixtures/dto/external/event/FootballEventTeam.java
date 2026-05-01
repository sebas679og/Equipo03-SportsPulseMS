package com.sportspulse.fixtures.dto.external.event;

/**
 * Data Transfer Object (DTO) representing a team associated with a match event.
 *
 * <p>This record is used to identify which team was involved in a specific incident (e.g., which
 * team scored a goal or received a booking) as reported by the external provider.
 *
 * @param id The unique identifier of the team.
 * @param name The display name of the team.
 */
public record FootballEventTeam(Long id, String name) {}
