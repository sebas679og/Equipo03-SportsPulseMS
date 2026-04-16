package com.sportspulse.teams.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/**
 * StadiumResponse Represents the details of a stadium. Contains basic information such as name,
 * address, city, capacity, and surface type.
 */
@Value
@Getter
@Builder
public class StadiumResponse {
  String name;
  String address;
  String city;
  int capacity;
  String surface;
}
