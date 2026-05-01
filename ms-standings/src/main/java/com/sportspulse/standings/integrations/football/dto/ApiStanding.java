package com.sportspulse.standings.integrations.football.dto;

/**
 * Standing Record representing a team's standing within a league.
 *
 * <p>Contains ranking information, team details, points, goal difference, group assignment, form,
 * status, and description. Also includes match statistics for all games, home games, and away
 * games, encapsulated in {@link ApiMatchStats}. Provides an update timestamp to indicate the last
 * refresh of data.
 */
public record ApiStanding(
    int rank,
    ApiTeam team,
    int points,
    int goalsDiff,
    String group,
    String form,
    String status,
    String description,
    ApiMatchStats all,
    ApiMatchStats home,
    ApiMatchStats away,
    String update) {}
