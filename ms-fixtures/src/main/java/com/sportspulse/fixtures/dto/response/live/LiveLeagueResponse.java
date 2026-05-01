package com.sportspulse.fixtures.dto.response.live;

/**
 * Data Transfer Object (DTO) providing minimal league identification for live match updates.
 *
 * <p>In a live context, this record provides the essential metadata needed to categorize a fixture
 * under its respective competition without the overhead of full league statistics or standings.
 *
 * @param id The unique identifier of the league.
 * @param name The official name of the competition (e.g., "Premier League", "La Liga").
 */
public record LiveLeagueResponse(Long id, String name) {}
