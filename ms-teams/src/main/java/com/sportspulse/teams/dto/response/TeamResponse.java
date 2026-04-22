package com.sportspulse.teams.dto.response;

import lombok.Builder;

@Builder
public record TeamResponse(
        Integer id,
        String name,
        String country,
        String logo,
        Integer founded,
        VenueResponse venue
) {
}
