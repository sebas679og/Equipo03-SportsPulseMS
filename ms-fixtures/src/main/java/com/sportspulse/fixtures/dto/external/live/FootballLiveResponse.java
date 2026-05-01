package com.sportspulse.fixtures.dto.external.live;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing the top-level response for live match data.
 *
 * <p>This record acts as the primary wrapper for the payload received from the external Football
 * API's live endpoint, mapping the core data array into a list of individual match items.
 *
 * @param response A list of {@link FootballLiveItem} containing real-time data for all matches
 *     currently being tracked.
 */
public record FootballLiveResponse(List<FootballLiveItem> response) {}
