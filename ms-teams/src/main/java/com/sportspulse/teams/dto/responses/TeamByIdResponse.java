package com.sportspulse.teams.dto.responses;

import lombok.Builder;
import lombok.Value;

/**
 * TeamResponse Represents the details of a football team. Contains basic information such as
 * identity, country, logo, foundation year, whether it is a national team, and its associated
 * stadium.
 */
@Value
@Builder
public class TeamByIdResponse {
  int id;
  String name;
  String country;
  String logo;
  int founded;
  boolean national;
  StadiumByIdResponse stadium;
}
