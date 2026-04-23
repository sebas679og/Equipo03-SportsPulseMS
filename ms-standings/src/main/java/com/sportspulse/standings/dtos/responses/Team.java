package com.sportspulse.standings.dtos.responses;

import lombok.Builder;
import lombok.Value;

/**
 * Team Data transfer object (DTO) representing a football team.
 *
 * <p>Contains the team's unique identifier, name, and logo URL. Built using Lombok annotations
 * {@link Value} and {@link Builder} to ensure immutability and provide a fluent builder pattern.
 */
@Value
@Builder
public class Team {
  int id;
  String name;
  String logo;
}
