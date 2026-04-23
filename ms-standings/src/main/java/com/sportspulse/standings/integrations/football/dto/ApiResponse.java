package com.sportspulse.standings.integrations.football.dto;

/**
 * Response Record representing the API response containing league information.
 *
 * <p>Encapsulates a {@link ApiLeague} object, providing structured access to league details and
 * associated standings data.
 */
public record ApiResponse(ApiLeague league) {}
