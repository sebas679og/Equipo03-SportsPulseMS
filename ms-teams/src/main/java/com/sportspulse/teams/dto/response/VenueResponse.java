package com.sportspulse.teams.dto.response;

import lombok.Builder;

@Builder
public record VenueResponse(
        String name,
        String city,
        Integer capacity
) {
}
