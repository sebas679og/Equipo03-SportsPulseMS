package com.sportspulse.fixtures.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFixture(
        long id,
        String referee,
        String timezone,
        Instant date,
        long timestamp,
        ApiPeriods periods,
        ApiVenue venue,
        ApiStatus status) {
}
