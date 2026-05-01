package com.sportspulse.standings.integrations.football.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Goals Record representing goal statistics for a team.
 *
 * <p>Contains the number of goals scored ("for") and goals conceded ("against"). Mapped from JSON
 * properties to ensure proper deserialization.
 */
public record ApiGoals(@JsonProperty("for") int goalsFor, int against) {}
