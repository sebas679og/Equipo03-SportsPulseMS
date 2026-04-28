package com.sportspulse.standings.dtos.responses.complements;

import lombok.Builder;
import lombok.Value;

/**
 * Standing Data transfer object (DTO) representing a team's standing in a league.
 *
 * <p>Contains ranking information, team details, accumulated points, match statistics (played, won,
 * drawn, lost), and goal-related data including goals scored, goals conceded, and goal difference.
 * Also includes the team's recent form as a string representation.
 *
 * <p>Built using Lombok annotations {@link Value} and {@link Builder} to ensure immutability and
 * provide a fluent builder pattern.
 */
@Value
@Builder
public class Standing {
  int rank;
  Team team;
  int points;
  int played;
  int won;
  int drawn;
  int lost;
  int goalsFor;
  int goalsAgainst;
  int goalDifference;
  String form;
}
