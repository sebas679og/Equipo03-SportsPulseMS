package com.sportspulse.standings.dtos.responses;

import lombok.Builder;
import lombok.Value;

/**
 * League Data transfer object (DTO) representing a football league.
 *
 * <p>Contains basic league details such as unique identifier, name, associated country, and season
 * year.
 *
 * <p>Built using Lombok annotations {@link Value} and {@link Builder} to ensure immutability and
 * provide a fluent builder pattern.
 */
@Value
@Builder
public class League {
  int id;
  String name;
  String country;
  int season;
}
