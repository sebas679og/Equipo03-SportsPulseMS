package com.sportspulse.leagues.dto.responses;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Standard API error response for leagues service. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeagueErrorResponse {

	private String error;
	private String message;
	private Instant timestamp;
}
