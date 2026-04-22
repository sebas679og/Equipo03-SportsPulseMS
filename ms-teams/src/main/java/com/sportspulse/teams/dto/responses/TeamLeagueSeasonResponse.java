package com.sportspulse.teams.dto.responses;

import lombok.Builder;
import lombok.Value;

/**
 * TeamLeagueSeasonResponse Data transfer object (DTO) representing a team's information within the
 * context of a league season.
 *
 * <p>Contains basic team details such as ID, name, country, logo, founding year, and associated
 * stadium information. Built using Lombok annotations for immutability and builder pattern.
 */
@Value
@Builder
public class TeamLeagueSeasonResponse {
  int id;
  String name;
  String country;
  String logo;
  int founded;
  StadiumLeagueSeasonResponse stadium;
}
