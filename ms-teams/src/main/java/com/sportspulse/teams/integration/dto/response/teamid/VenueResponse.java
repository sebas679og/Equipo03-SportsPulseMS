package com.sportspulse.teams.integration.dto.response.teamid;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/**
 * VenueResponse Represents the details of a sports venue. Contains information such as identity,
 * location, capacity, surface type, and image reference.
 */
@Value
@Getter
@Builder
public class VenueResponse {
  int id;
  String name;
  String address;
  String city;
  int capacity;
  String surface;
  String image;
}
