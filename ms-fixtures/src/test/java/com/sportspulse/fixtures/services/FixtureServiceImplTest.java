package com.sportspulse.fixtures.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sportspulse.fixtures.dtos.requests.FixturesQueryParamsRequest;
import com.sportspulse.fixtures.dtos.responses.fixtures.Fixture;
import com.sportspulse.fixtures.dtos.responses.fixtures.FixturesResponse;
import com.sportspulse.fixtures.dtos.responses.fixtures.Status;
import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomNotFoundException;
import com.sportspulse.fixtures.exceptions.CustomTooManyRequestsException;
import com.sportspulse.fixtures.integration.football.FootballClient;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureData;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import com.sportspulse.fixtures.utils.mappers.FixtureMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FixtureServiceImpl")
class FixtureServiceImplTest {

    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock private FootballClient footballClient;

    @Mock private FixtureMapper fixtureMapper;

    @InjectMocks private FixtureServiceImpl fixtureService;

    private FixturesQueryParamsRequest queryParams;
    private ApiFixtureData apiFixtureData;
    private Fixture mappedFixture;

    @BeforeEach
    void setUp() {
        queryParams =
                FixturesQueryParamsRequest.builder()
                        .league(39)
                        .team(33)
                        .date(LocalDate.of(2026, 6, 27))
                        .status(null)
                        .build();

        apiFixtureData = mockApiFixtureData();
        mappedFixture =
                Fixture.builder()
                        .id(1L)
                        .status(Status.builder().shortName("NS").longName("Not Started").build())
                        .build();
    }

    private ApiFixtureData mockApiFixtureData() {
        return Mockito.mock(ApiFixtureData.class);
    }

    private ApiFixtureResponse responseWithErrors(JsonNode errors) {
        return new ApiFixtureResponse(
                "fixtures", null, errors, 1, null, List.of(apiFixtureData));
    }

    private ApiFixtureResponse responseWithFixtures(JsonNode errors, List<ApiFixtureData> fixtures) {
        return new ApiFixtureResponse("fixtures", null, errors, fixtures.size(), null, fixtures);
    }

    @Nested
    @DisplayName("getFixtures - happy path")
    class HappyPath {

        @Test
        @DisplayName("returns mapped fixtures when API responds with data and no errors")
        void returnsMappedFixturesWhenApiRespondsSuccessfully() {
            ArrayNode emptyErrors = objectMapper.createArrayNode();
            ApiFixtureResponse apiResponse =
                    responseWithFixtures(emptyErrors, List.of(apiFixtureData));

            when(footballClient.getFixtures(39, 33, queryParams.getDate(), null))
                    .thenReturn(apiResponse);
            when(fixtureMapper.toFixture(apiFixtureData)).thenReturn(mappedFixture);

            FixturesResponse result = fixtureService.getFixtures(queryParams);

            assertThat(result.getData()).containsExactly(mappedFixture);
            verify(footballClient).getFixtures(39, 33, queryParams.getDate(), null);
            verify(fixtureMapper).toFixture(apiFixtureData);
        }

        @Test
        @DisplayName("returns mapped fixtures when errors node is null")
        void returnsMappedFixturesWhenErrorsNodeIsNull() {
            ApiFixtureResponse apiResponse =
                    responseWithFixtures(null, List.of(apiFixtureData));

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);
            when(fixtureMapper.toFixture(apiFixtureData)).thenReturn(mappedFixture);

            FixturesResponse result = fixtureService.getFixtures(queryParams);

            assertThat(result.getData()).hasSize(1);
        }

        @Test
        @DisplayName("maps every element of a multi-fixture response")
        void mapsEveryFixtureInTheResponse() {
            ApiFixtureData second = mockApiFixtureData();
            Fixture secondMapped = Fixture.builder().id(2L).build();
            ArrayNode emptyErrors = objectMapper.createArrayNode();

            ApiFixtureResponse apiResponse =
                    responseWithFixtures(emptyErrors, List.of(apiFixtureData, second));

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);
            when(fixtureMapper.toFixture(apiFixtureData)).thenReturn(mappedFixture);
            when(fixtureMapper.toFixture(second)).thenReturn(secondMapped);

            FixturesResponse result = fixtureService.getFixtures(queryParams);

            assertThat(result.getData()).containsExactly(mappedFixture, secondMapped);
        }

        @Test
        @DisplayName("passes through query params untouched to the client")
        void passesQueryParamsThroughToClient() {
            ArrayNode emptyErrors = objectMapper.createArrayNode();
            ApiFixtureResponse apiResponse =
                    responseWithFixtures(emptyErrors, List.of(apiFixtureData));
            when(footballClient.getFixtures(
                    queryParams.getLeague(),
                    queryParams.getTeam(),
                    queryParams.getDate(),
                    queryParams.getStatus()))
                    .thenReturn(apiResponse);
            when(fixtureMapper.toFixture(apiFixtureData)).thenReturn(mappedFixture);

            fixtureService.getFixtures(queryParams);

            verify(footballClient)
                    .getFixtures(
                            queryParams.getLeague(),
                            queryParams.getTeam(),
                            queryParams.getDate(),
                            queryParams.getStatus());
        }
    }

    @Nested
    @DisplayName("getFixtures - not found")
    class NotFoundCases {

        @Test
        @DisplayName("throws CustomNotFoundException when response list is empty")
        void throwsNotFoundWhenResponseListIsEmpty() {
            ArrayNode emptyErrors = objectMapper.createArrayNode();
            ApiFixtureResponse apiResponse = responseWithFixtures(emptyErrors, List.of());

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);

            assertThatThrownBy(() -> fixtureService.getFixtures(queryParams))
                    .isInstanceOf(CustomNotFoundException.class)
                    .hasMessage("Fixture not found in Api-football");

            verifyNoInteractions(fixtureMapper);
        }

        @Test
        @DisplayName("throws CustomNotFoundException when response list is null")
        void throwsNotFoundWhenResponseListIsNull() {
            ArrayNode emptyErrors = objectMapper.createArrayNode();
            ApiFixtureResponse apiResponse =
                    new ApiFixtureResponse("fixtures", null, emptyErrors, 0, null, null);

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);

            assertThatThrownBy(() -> fixtureService.getFixtures(queryParams))
                    .isInstanceOf(CustomNotFoundException.class)
                    .hasMessage("Fixture not found in Api-football");

            verifyNoInteractions(fixtureMapper);
        }
    }

    @Nested
    @DisplayName("getFixtures - Api-Football error handling")
    class ApiFootballErrorHandling {

        @Test
        @DisplayName("throws CustomTooManyRequestsException on daily rate limit (requests key)")
        void throwsTooManyRequestsOnDailyRateLimit() {
            ObjectNode errors = objectMapper.createObjectNode();
            errors.put("requests", "Too many requests");
            ApiFixtureResponse apiResponse = responseWithErrors(errors);

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);

            assertThatThrownBy(() -> fixtureService.getFixtures(queryParams))
                    .isInstanceOf(CustomTooManyRequestsException.class)
                    .hasMessage(
                            "The daily request limit to Api-Football has been "
                                    + "reached, please try again tomorrow");

            verifyNoInteractions(fixtureMapper);
        }

        @Test
        @DisplayName("throws CustomTooManyRequestsException with plan message on season limit")
        void throwsTooManyRequestsWithPlanMessageOnSeasonLimit() {
            ObjectNode errors = objectMapper.createObjectNode();
            errors.put("plan", "Season request limit reached");
            ApiFixtureResponse apiResponse = responseWithErrors(errors);

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);

            assertThatThrownBy(() -> fixtureService.getFixtures(queryParams))
                    .isInstanceOf(CustomTooManyRequestsException.class)
                    .hasMessage("Season request limit reached");

            verifyNoInteractions(fixtureMapper);
        }

        @Test
        @DisplayName("prioritizes 'requests' over 'plan' when both keys are present")
        void prioritizesRequestsOverPlanWhenBothPresent() {
            ObjectNode errors = objectMapper.createObjectNode();
            errors.put("requests", "Too many requests");
            errors.put("plan", "Season request limit reached");
            ApiFixtureResponse apiResponse = responseWithErrors(errors);

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);

            assertThatThrownBy(() -> fixtureService.getFixtures(queryParams))
                    .isInstanceOf(CustomTooManyRequestsException.class)
                    .hasMessage(
                            "The daily request limit to Api-Football has been "
                                    + "reached, please try again tomorrow");
        }

        @Test
        @DisplayName("throws CustomBadGatewayException for any other error shape")
        void throwsBadGatewayForOtherErrorShapes() {
            ObjectNode errors = objectMapper.createObjectNode();
            errors.put("token", "Invalid API token");
            ApiFixtureResponse apiResponse = responseWithErrors(errors);

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);

            assertThatThrownBy(() -> fixtureService.getFixtures(queryParams))
                    .isInstanceOf(CustomBadGatewayException.class)
                    .hasMessage(
                            "An error occurred while processing the request to Api-Football. "
                                    + "Please try again later.");

            verifyNoInteractions(fixtureMapper);
        }

        @Test
        @DisplayName("does not throw when errors is a non-empty array node (treated as no error)")
        void doesNotThrowWhenErrorsIsNonEmptyArrayNode() {
            ArrayNode errors = objectMapper.createArrayNode();
            errors.add("some-array-element");
            ApiFixtureResponse apiResponse = responseWithFixtures(errors, List.of(apiFixtureData));

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);
            when(fixtureMapper.toFixture(apiFixtureData)).thenReturn(mappedFixture);

            FixturesResponse result = fixtureService.getFixtures(queryParams);

            assertThat(result.getData()).containsExactly(mappedFixture);
        }

        @Test
        @DisplayName("does not call the mapper when an Api-Football error is raised")
        void doesNotCallMapperWhenErrorIsRaised() {
            ObjectNode errors = objectMapper.createObjectNode();
            errors.put("token", "Invalid API token");
            ApiFixtureResponse apiResponse = responseWithErrors(errors);

            when(footballClient.getFixtures(any(), any(), any(), any())).thenReturn(apiResponse);

            assertThatThrownBy(() -> fixtureService.getFixtures(queryParams))
                    .isInstanceOf(CustomBadGatewayException.class);

            verify(fixtureMapper, never()).toFixture(any());
        }
    }
}