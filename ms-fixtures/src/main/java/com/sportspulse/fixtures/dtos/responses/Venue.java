package com.sportspulse.fixtures.dtos.responses;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Venue {
    String name;
    String city;
}
