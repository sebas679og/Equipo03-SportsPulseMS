package com.sportspulse.fixtures.dto.external.event;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing the wrapper response for fixture events.
 *
 * <p>This record serves as the top-level container for data returned by the external Football API
 * event endpoint, mapping the root JSON array to a list of domain-specific event items.
 *
 * @param response A list of {@link FootballFixtureEventItem} containing all incidents recorded for
 *     a specific match.
 */
public record FootballFixtureEventsResponse(List<FootballFixtureEventItem> response) {}
