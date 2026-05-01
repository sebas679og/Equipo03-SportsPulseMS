package com.sportspulse.fixtures.dto.external.event;

/**
 * Data Transfer Object (DTO) representing a player involved in a specific match event.
 *
 * <p>This record is used to map player data from the external Football API, typically identifying
 * the player who scored a goal, received a card, or was part of a substitution.
 *
 * @param id The unique identifier of the player assigned by the external provider.
 * @param name The full name of the player.
 */
public record FootballEventPlayer(Long id, String name) {}
