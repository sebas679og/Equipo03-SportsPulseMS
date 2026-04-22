package com.sportspulse.standings.integrations.football.dto;

/**
 * Standing Record representing a team's standing within a league.
 *
 * <p>Contains ranking information, team details, points, goal difference, group assignment, form,
 * status, and description. Also includes match statistics for all games, home games, and away
 * games, encapsulated in {@link MatchStats}. Provides an update timestamp to indicate the last
 * refresh of data.
 */
public record Standing(
    int rank,
    Team team,
    int points,
    int goalsDiff,
    String group,
    String form,
    String status,
    String description,
    MatchStats all,
    MatchStats home,
    MatchStats away,
    String update) {}
