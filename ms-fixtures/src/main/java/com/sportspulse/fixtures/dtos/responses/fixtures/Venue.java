package com.sportspulse.fixtures.dtos.responses.fixtures;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Venue {
    String name;
    String city;
}
