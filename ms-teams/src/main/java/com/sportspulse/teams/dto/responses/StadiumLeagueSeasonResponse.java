package com.sportspulse.teams.dto.responses;

import lombok.Builder;
import lombok.Value;

/**
 * StadiumLeagueSeasonResponse Data transfer object (DTO) representing stadium information within
 * the context of a league season.
 *
 * <p>Contains basic stadium details such as name, city, and capacity. Built using Lombok
 * annotations for immutability and builder pattern.
 */
@Value
@Builder
public class StadiumLeagueSeasonResponse {
  String name;
  String city;
  int capacity;
}
