package com.sportspulse.standings.dtos.responses.complements;

import lombok.Builder;
import lombok.Value;

/**
 * StandingTeam Data transfer object (DTO) representing a team within the standings context.
 *
 * <p>Contains the team's unique identifier and name, providing essential information for ranking
 * and classification operations.
 *
 * <p>Built using Lombok annotations {@link Value} and {@link Builder} to ensure immutability and
 * provide a fluent builder pattern.
 */
@Value
@Builder
public class StandingTeam {
  int id;
  String name;
}
