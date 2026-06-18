package com.sportspulse.fixtures.dtos.responses;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class League {
    Long id;
    String name;
    String round;
}
