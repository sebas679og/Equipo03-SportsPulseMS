package com.sportspulse.leagues.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** League data from API-Football. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiFootballLeague {

  private Integer id;
  private String name;
  private String type;
  private String logo;
}
