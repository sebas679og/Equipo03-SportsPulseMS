package com.sportspulse.fixtures.dto.response.event;

/**
 * Data Transfer Object (DTO) representing a player involved in a specific match event.
 *
 * <p>This record is used to identify the individual associated with an event (such as a goal,
 * substitution, or booking) within the context of a live or finished match.
 *
 * @param id The unique identifier of the player.
 * @param name The display name of the player.
 */
public record EventPlayerResponse(Long id, String name) {}
