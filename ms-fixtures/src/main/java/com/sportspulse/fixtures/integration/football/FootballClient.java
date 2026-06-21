package com.sportspulse.fixtures.integration.football;

import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import com.sportspulse.fixtures.utils.Status;

import java.time.LocalDate;

public interface FootballClient {

    ApiFixtureResponse getFixtures(Integer league, Integer team, LocalDate date, Status status);
}
