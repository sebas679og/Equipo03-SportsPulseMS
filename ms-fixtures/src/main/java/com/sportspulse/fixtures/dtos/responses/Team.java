package com.sportspulse.fixtures.dtos.responses;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Team {
    Long id;
    String name;
    String logo;
    Integer goals;
}
