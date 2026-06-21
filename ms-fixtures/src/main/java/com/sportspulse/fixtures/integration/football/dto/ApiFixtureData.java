package com.sportspulse.fixtures.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFixtureData(
        ApiFixture fixture,
        ApiLeague league,
        ApiTeams teams,
        ApiGoals goals,
        ApiScore score) {}
