package com.sportspulse.fixtures.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiLeague(
        long id,
        String name,
        String country,
        String logo,
        String flag,
        int season,
        String round) {}
