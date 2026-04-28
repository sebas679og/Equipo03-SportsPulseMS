package com.sportspulse.standings.dtos.responses.complements;

import lombok.Builder;
import lombok.Value;

/**
 * LeagueTeam Data transfer object (DTO) representing a team within a league context.
 *
 * <p>Contains the team's unique identifier and name, providing essential information for
 * classification or standings operations.
 *
 * <p>Built using Lombok annotations {@link Value} and {@link Builder} to ensure immutability and
 * provide a fluent builder pattern.
 */
@Value
@Builder
public class LeagueTeam {
  int id;
  String name;
}
