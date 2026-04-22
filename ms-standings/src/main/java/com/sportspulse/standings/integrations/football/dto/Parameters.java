package com.sportspulse.standings.integrations.football.dto;

/**
 * Parameters Record representing query parameters for API requests.
 *
 * <p>Contains the league identifier and the season year used to filter or retrieve specific data
 * from the API.
 */
public record Parameters(String league, String season) {}
