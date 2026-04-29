package com.sportspulse.leagues.integration.football.dto;

/**
 * ApiPaging Data transfer object (DTO) representing pagination details provided by the external
 * football API.
 *
 * <p>Contains the current page number and the total number of pages, offering metadata to support
 * paginated data retrieval.
 */
public record ApiPaging(int current, int total) {}
