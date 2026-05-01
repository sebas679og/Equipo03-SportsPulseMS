package com.sportspulse.fixtures.dto.response.event;

/**
 * Data Transfer Object (DTO) representing a specific event that occurred during a match.
 *
 * <p>This record details key match incidents such as goals, cards, and substitutions. It includes
 * temporal data, the nature of the event, and the primary and secondary participants involved.
 *
 * @param elapsed The match minute when the event occurred.
 * @param type The category of the event (e.g., "Goal", "Card", "Subst").
 * @param detail Specific information about the event (e.g., "Yellow Card", "Normal Goal").
 * @param team The {@link EventTeamResponse} representing the team associated with the event.
 * @param player The {@link EventPlayerResponse} representing the main player involved.
 * @param assist The {@link EventPlayerResponse} representing the player who assisted (if
 *     applicable).
 */
public record FixtureEventResponse(
    Integer elapsed,
    String type,
    String detail,
    EventTeamResponse team,
    EventPlayerResponse player,
    EventPlayerResponse assist) {}
