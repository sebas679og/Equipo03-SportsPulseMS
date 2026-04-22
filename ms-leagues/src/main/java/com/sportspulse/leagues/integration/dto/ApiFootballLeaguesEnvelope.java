package com.sportspulse.leagues.integration.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Root response from API-Football /leagues endpoint. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiFootballLeaguesEnvelope {

	private List<ApiFootballLeagueWrapper> response;
}
