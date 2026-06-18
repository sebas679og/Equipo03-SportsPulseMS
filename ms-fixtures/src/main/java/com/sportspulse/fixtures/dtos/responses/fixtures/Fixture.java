package com.sportspulse.fixtures.dtos.responses.fixtures;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class Fixture {
    Long id;
    Instant date;
    Status status;
    League league;
    Team homeTeam;
    Team awayTeam;
    Venue venue;
}
