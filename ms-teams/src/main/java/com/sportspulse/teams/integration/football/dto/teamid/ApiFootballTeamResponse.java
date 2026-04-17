package com.sportspulse.teams.integration.football.dto.teamid;

import java.util.List;
import java.util.Map;

/**
 * ApiFootballTeamResponse Represents the API response structure for football team data. Contains
 * request metadata, parameters, errors, pagination details, and the list of team and venue response
 * items.
 */
public record ApiFootballTeamResponse(
    String get,
    Map<String, String> parameters,
    List<Object> errors,
    int results,
    ApiPagingResponse paging,
    List<ApiResponseItem> response) {}
