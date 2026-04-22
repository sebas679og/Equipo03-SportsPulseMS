package com.sportspulse.leagues.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Season data from API-Football. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiFootballSeason {

	private Integer year;
	private String start;
	private String end;
	private Boolean current;
}
