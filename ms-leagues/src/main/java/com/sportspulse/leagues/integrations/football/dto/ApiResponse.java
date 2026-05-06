package com.sportspulse.leagues.integrations.football.dto;

import java.util.List;

/**
 * ApiResponse Data transfer object (DTO) representing the response structure provided by the
 * external football API.
 *
 * <p>Encapsulates details about the {@link ApiLeague}, the associated {@link ApiCountry}, and a
 * list of {@link ApiSeason} objects describing the seasons covered for the league.
 */
public record ApiResponse(ApiLeague league, ApiCountry country, List<ApiSeason> seasons) {}
