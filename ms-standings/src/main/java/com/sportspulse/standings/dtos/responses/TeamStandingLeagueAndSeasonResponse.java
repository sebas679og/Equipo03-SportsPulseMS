package com.sportspulse.standings.dtos.responses;

import com.sportspulse.standings.dtos.responses.complements.LeagueTeam;
import com.sportspulse.standings.dtos.responses.complements.StandingTeam;
import lombok.Builder;
import lombok.Value;

/**
 * TeamStandingLeagueAndSeasonResponse Data transfer object (DTO) representing a team's standing
 * within a specific league and season context.
 *
 * <p>Encapsulates details about the {@link StandingTeam}, the {@link LeagueTeam}, and season year,
 * along with ranking information, accumulated points, matches played, recent form, and descriptive
 * notes about the standing.
 *
 * <p>Built using Lombok annotations {@link Value} and {@link Builder} to ensure immutability and
 * provide a fluent builder pattern.
 */
@Value
@Builder
public class TeamStandingLeagueAndSeasonResponse {
  StandingTeam team;
  LeagueTeam league;
  int season;
  int rank;
  int points;
  int played;
  String form;
  String description;
}
