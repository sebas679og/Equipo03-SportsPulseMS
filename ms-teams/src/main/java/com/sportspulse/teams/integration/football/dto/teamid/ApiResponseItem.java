package com.sportspulse.teams.integration.football.dto.teamid;

/**
 * ResponseItem Represents a composite response containing both team and venue details. Serves as a
 * wrapper object to group related entities in service responses.
 */
public record ApiResponseItem(ApiTeamResponse team, ApiVenueResponse venue) {}
