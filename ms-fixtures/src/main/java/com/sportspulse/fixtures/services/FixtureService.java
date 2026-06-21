package com.sportspulse.fixtures.services;

import com.sportspulse.fixtures.dtos.requests.FixturesQueryParamsRequest;
import com.sportspulse.fixtures.dtos.responses.fixtures.FixturesResponse;

public interface FixtureService {

    FixturesResponse getFixtures(FixturesQueryParamsRequest queryParams);
}
