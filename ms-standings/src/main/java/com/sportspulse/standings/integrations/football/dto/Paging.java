package com.sportspulse.standings.integrations.football.dto;

/**
 * Paging Record representing pagination details for API responses.
 *
 * <p>Contains the current page number and the total number of pages, providing metadata to support
 * paginated data retrieval.
 */
public record Paging(int current, int total) {}
