package com.sportspulse.fixtures.dtos.responses.fixtures;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FixturesResponse {
    List<Fixture> data;
}
