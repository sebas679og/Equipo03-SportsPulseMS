package com.sportspulse.teams.integration.dto.response.teamid;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/**
 * PagingResponse
 * Represents pagination details for API responses.
 * Contains information about the current page and the total number of pages.
 */
@Value
@Getter
@Builder
public class PagingResponse {
    int current;
    int total;
}

