package com.sportspulse.fixtures.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Status {

    @JsonProperty("short")
    String shortName;

    @JsonProperty("long")
    String longName;
}
