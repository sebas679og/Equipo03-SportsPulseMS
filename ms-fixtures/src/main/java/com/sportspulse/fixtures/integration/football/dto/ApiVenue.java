package com.sportspulse.fixtures.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiVenue(long id, String name, String city) {}
