package com.sportspulse.fixtures.services;

import com.sportspulse.fixtures.dtos.requests.FixturesQueryParamsRequest;
import com.sportspulse.fixtures.dtos.responses.fixtures.FixturesResponse;
import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomNotFoundException;
import com.sportspulse.fixtures.exceptions.CustomTooManyRequestsException;
import com.sportspulse.fixtures.integration.football.FootballClient;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureData;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import com.sportspulse.fixtures.utils.mappers.FixtureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixtureServiceImpl implements FixtureService{

    private final FootballClient footballClient;
    private final FixtureMapper fixtureMapper;

    @Override
    public FixturesResponse getFixtures(FixturesQueryParamsRequest queryParams) {
        ApiFixtureResponse api = footballClient.getFixtures(
                queryParams.getLeague(),
                queryParams.getTeam(),
                queryParams.getDate(),
                queryParams.getStatus()
        );

        handleApiFootballErrors(api);
        List<ApiFixtureData> fixtures = api.response();

        if (fixtures == null || fixtures.isEmpty()) {
            throw new CustomNotFoundException("Fixture not found in Api-football");
        }

        return FixturesResponse.builder()
                .data(fixtures.stream()
                        .map(fixtureMapper::toFixture)
                        .toList())
                .build();
    }

    private void handleApiFootballErrors(ApiFixtureResponse api) {
        if (api.errors() == null || api.errors().isEmpty()) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> errorDetail = (Map<String, Object>) api.errors().getFirst();

        if (errorDetail.containsKey("requests")) {
            log.warn(
                    "Api-Football 429 Rate Limit exceeded. "
                            + "Available requests have been exhausted. Body: {}",
                    errorDetail);
            throw new CustomTooManyRequestsException(
                    "The daily request limit to Api-Football has been "
                            + "reached, please try again tomorrow");
        } else if (errorDetail.containsKey("plan")) {
            String planMessage = (String) errorDetail.get("plan");
            log.warn(
                    "Api-Football, the request limit per season has been exceeded. "
                            + "Available requests have been exhausted. Body: {}",
                    planMessage);
            throw new CustomTooManyRequestsException(planMessage);
        } else {
            log.error("Api-Football returned errors in the response. Body: {}", errorDetail);
            throw new CustomBadGatewayException(
                    "An error occurred while processing the request to Api-Football. "
                            + "Please try again later.");
        }
    }
}
