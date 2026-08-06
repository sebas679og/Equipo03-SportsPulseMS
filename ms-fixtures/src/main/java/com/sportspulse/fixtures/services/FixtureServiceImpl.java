package com.sportspulse.fixtures.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.sportspulse.fixtures.dtos.requests.FixturesQueryParamsRequest;
import com.sportspulse.fixtures.dtos.responses.fixtures.FixturesResponse;
import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomBadRequestException;
import com.sportspulse.fixtures.exceptions.CustomNotFoundException;
import com.sportspulse.fixtures.exceptions.CustomTooManyRequestsException;
import com.sportspulse.fixtures.integration.football.FootballClient;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureData;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import com.sportspulse.fixtures.utils.mappers.FixtureMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixtureServiceImpl implements FixtureService{

    private final FootballClient footballClient;
    private final FixtureMapper fixtureMapper;

    @Override
    public FixturesResponse getFixtures(FixturesQueryParamsRequest queryParams) {
        boolean noFilters = queryParams.getLeague() == null
                && queryParams.getTeam() == null
                && queryParams.getStatus() == null;

        LocalDate effectiveDate = (noFilters && queryParams.getDate() == null)
                ? LocalDate.now()
                : queryParams.getDate();

        if (queryParams.getTeam() != null && queryParams.getSeason() == null
                || queryParams.getLeague() != null && queryParams.getSeason() == null){
            throw new CustomBadRequestException("The season field is required to search for leagues and teams");
        }


        ApiFixtureResponse api = footballClient.getFixtures(
                queryParams.getLeague(),
                queryParams.getTeam(),
                effectiveDate,
                queryParams.getStatus(),
                queryParams.getSeason()
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
        JsonNode errors = api.errors();
        if (errors == null || errors.isEmpty() || errors.isArray()) {
            return;
        }

        if (errors.has("requests")) {
            log.warn(
                    "Api-Football 429 Rate Limit exceeded. "
                            + "Available requests have been exhausted. Body: {}",
                    errors);
            throw new CustomTooManyRequestsException(
                    "The daily request limit to Api-Football has been "
                            + "reached, please try again tomorrow");
        } else if (errors.has("plan")) {
            String planMessage = errors.get("plan").asText();
            log.warn(
                    "Api-Football, the request limit per season has been exceeded. "
                            + "Available requests have been exhausted. Body: {}",
                    planMessage);
            throw new CustomTooManyRequestsException(planMessage);
        } else {
            log.error("Api-Football returned errors in the response. Body: {}", errors);
            throw new CustomBadGatewayException(
                    "An error occurred while processing the request to Api-Football. "
                            + "Please try again later.");
        }
    }
}
