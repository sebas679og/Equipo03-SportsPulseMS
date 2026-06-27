package com.sportspulse.fixtures.integartion.football;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomServiceUnavailableException;
import com.sportspulse.fixtures.integration.football.FootballClientImpl;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import com.sportspulse.fixtures.utils.Status;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.all;
import static org.instancio.Select.field;

@DisplayName("FootballClientImpl Unit Tests")
class FootballClientImplTest {

    private MockWebServer mockWebServer;
    private FootballClientImpl footballClient;
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient =
                WebClient.builder()
                        .baseUrl(mockWebServer.url("/").toString())
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();

        footballClient = new FootballClientImpl(webClient);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    private static final int SAMPLE_SIZE = 2;

    private ApiFixtureResponse buildFixtureResponse() {
        return Instancio.of(ApiFixtureResponse.class)
                .generate(field(ApiFixtureResponse::response), gen -> gen.collection().size(SAMPLE_SIZE))
                .supply(all(Instant.class), () -> Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .set(field(ApiFixtureResponse::parameters), com.fasterxml.jackson.databind.node.NullNode.getInstance())
                .set(field(ApiFixtureResponse::errors), com.fasterxml.jackson.databind.node.NullNode.getInstance())
                .create();
    }

    private ApiFixtureResponse minimalResponse() {
        return new ApiFixtureResponse("fixtures", null, null, 0, null, List.of());
    }

    private MockResponse jsonResponse(Object body) throws Exception {
        return new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(body));
    }

    private MockResponse emptyResponse(int status) {
        return new MockResponse().setResponseCode(status);
    }

    @Nested
    @DisplayName("getFixtures - successful response")
    class GetFixtures {

        @Test
        @DisplayName("returns the exact deserialized response when the server responds with fixtures")
        void shouldReturnExactResponse_whenFixturesExist() throws Exception {
            ApiFixtureResponse expected = buildFixtureResponse();
            mockWebServer.enqueue(jsonResponse(expected));

            ApiFixtureResponse result = footballClient.getFixtures(null, null, null, null);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("deserializes a fixture list of the expected size")
        void shouldDeserializeFixtureListOfExpectedSize() throws Exception {
            ApiFixtureResponse expected = buildFixtureResponse();
            mockWebServer.enqueue(jsonResponse(expected));

            ApiFixtureResponse result = footballClient.getFixtures(null, null, null, null);

            assertThat(result.response()).hasSize(SAMPLE_SIZE);
        }

        @Test
        @DisplayName("sends every query param when league, team, date and status are provided")
        void shouldSendCorrectQueryParams_whenAllParamsProvided() throws Exception {
            mockWebServer.enqueue(jsonResponse(minimalResponse()));
            LocalDate date = LocalDate.now();
            Status status = Status.FT;

            footballClient.getFixtures(223, 23, date, status);

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getPath())
                    .contains("league=223")
                    .contains("team=23")
                    .contains(String.format("date=%s", date))
                    .contains(String.format("status=%s", status.name().toLowerCase(Locale.ROOT)));
        }

        @Test
        @DisplayName("omits league, team and status when null, but always sends date")
        void shouldOmitOptionalQueryParams_whenNull() throws Exception {
            mockWebServer.enqueue(jsonResponse(minimalResponse()));

            footballClient.getFixtures(null, null, null, null);

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getPath())
                    .doesNotContain("league=")
                    .doesNotContain("team=")
                    .doesNotContain("status=")
                    .contains("date");
        }
    }

    // ===========================================================================
    // 5xx / unexpected status code handling
    // ===========================================================================
    @Nested
    @DisplayName("Non-2xx server responses")
    class ServerErrorTest {

        @Test
        @DisplayName("throws CustomServiceUnavailableException on 204 No Content")
        void shouldThrowServiceUnavailable_on204() {
            mockWebServer.enqueue(emptyResponse(204));

            assertThatThrownBy(
                    () -> footballClient.getFixtures(123, 21, LocalDate.now(), Status.FT))
                    .isInstanceOf(CustomServiceUnavailableException.class);
        }

        @Test
        @DisplayName("throws CustomServiceUnavailableException on 499")
        void shouldThrowServiceUnavailable_on499() {
            mockWebServer.enqueue(emptyResponse(499));

            assertThatThrownBy(
                    () -> footballClient.getFixtures(123, 21, LocalDate.now(), Status.FT))
                    .isInstanceOf(CustomServiceUnavailableException.class);
        }

        @Test
        @DisplayName("throws CustomServiceUnavailableException on 500")
        void shouldThrowServiceUnavailable_on500() {
            mockWebServer.enqueue(emptyResponse(500));

            assertThatThrownBy(
                    () -> footballClient.getFixtures(123, 21, LocalDate.now(), Status.FT))
                    .isInstanceOf(CustomServiceUnavailableException.class);
        }
    }

    // ===========================================================================
    // Empty body handling
    // ===========================================================================
    @Nested
    @DisplayName("Empty response body")
    class EmptyBodyTest {

        @Test
        @DisplayName("throws CustomServiceUnavailableException when body is the JSON literal null")
        void shouldThrowServiceUnavailable_whenBodyIsNull() {
            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody("null"));

            assertThatThrownBy(
                    () -> footballClient.getFixtures(123, 21, LocalDate.now(), Status.FT))
                    .isInstanceOf(CustomServiceUnavailableException.class);
        }
    }

    // ===========================================================================
    // Connection failure handling
    // ===========================================================================
    @Nested
    @DisplayName("Connection failures")
    class ConnectionFailureTest {

        @Test
        @DisplayName("throws CustomBadGatewayException when the server is unreachable")
        void shouldThrowBadGatewayWhenServerIsUnreachable() throws Exception {
            mockWebServer.shutdown();

            assertThatThrownBy(
                    () -> footballClient.getFixtures(123, 21, LocalDate.now(), Status.FT))
                    .isInstanceOf(CustomBadGatewayException.class);
        }
    }
}