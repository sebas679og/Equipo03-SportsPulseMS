package com.sportspulse.fixtures.dto.external.event;

/**
 * Data Transfer Object (DTO) representing a specific event occurrence during a football match.
 *
 * <p>This record consolidates all details related to a single match incident, including the time it
 * occurred, the team and players involved, and the nature of the event (e.g., Goal, Card,
 * Substitution).
 *
 * @param time The {@link FootballEventTime} indicating when the event happened.
 * @param team The {@link FootballEventTeam} representing the team responsible for the event.
 * @param player The {@link FootballEventPlayer} who performed the main action.
 * @param assist The {@link FootballEventPlayer} who provided the assist, if applicable.
 * @param type The category of the event (e.g., "Goal", "Card", "subst").
 * @param detail Specific information about the event (e.g., "Normal Goal", "Yellow Card").
 */
public record FootballFixtureEventItem(
    FootballEventTime time,
    FootballEventTeam team,
    FootballEventPlayer player,
    FootballEventPlayer assist,
    String type,
    String detail) {}
