package com.sportspulse.fixtures.dto.external;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing the complete response from a fixture search query.
 *
 * <p>This record acts as the root container for API responses that return multiple fixtures. It
 * provides both the list of match data and metadata regarding the total number of items found.
 *
 * @param response A list of {@link FootballFixtureItem} containing the detailed information for
 *     each match found.
 * @param results The total count of fixture items included in the response.
 */
public record FootballFixtureResponse(List<FootballFixtureItem> response, int results) {}
