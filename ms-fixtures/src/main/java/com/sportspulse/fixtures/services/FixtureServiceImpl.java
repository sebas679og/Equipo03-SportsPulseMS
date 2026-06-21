package com.sportspulse.fixtures.services;

import com.sportspulse.fixtures.dtos.requests.FixturesQueryParamsRequest;
import com.sportspulse.fixtures.dtos.responses.fixtures.FixturesResponse;
import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomTooManyRequestsException;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class FixtureServiceImpl implements FixtureService{


    @Override
    public FixturesResponse getFixtures(FixturesQueryParamsRequest queryParams) {
        return null;
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
