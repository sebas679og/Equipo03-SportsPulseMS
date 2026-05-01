package com.sportspulse.teams.integration.football.dto.teamid;

/**
 * PagingResponse Represents pagination details for API responses. Contains information about the
 * current page and the total number of pages.
 */
public record ApiPagingResponse(int current, int total) {}
