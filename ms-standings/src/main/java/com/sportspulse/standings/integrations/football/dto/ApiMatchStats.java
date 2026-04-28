package com.sportspulse.standings.integrations.football.dto;

/**
 * MatchStats Record representing match statistics for a team.
 *
 * <p>Contains the number of matches played, wins, draws, and losses, along with associated goal
 * statistics encapsulated in {@link ApiGoals}.
 */
public record ApiMatchStats(int played, int win, int draw, int lose, ApiGoals goals) {}
