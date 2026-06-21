package com.sportspulse.fixtures.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiStatus(
        @JsonProperty("long") String longName,
        @JsonProperty("short") String shortName,
        Integer elapsed,
        Integer extra) {}