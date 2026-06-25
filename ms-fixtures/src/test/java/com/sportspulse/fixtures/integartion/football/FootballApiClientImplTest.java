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

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FootballApiClientImpl Unit Tests")
class FootballApiClientImplTest {

    private MockWebServer mockWebServer;
    private FootballClientImpl footballClient;
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() throws Exception{
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

    private ApiFixtureResponse buildFixtureResponse(){
        return Instancio.create(ApiFixtureResponse.class);
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
    @DisplayName("getFixtures")
    class GetFixtures {

        @Test
        void shouldReturnResponse_whenDateProvided() throws Exception {
            ApiFixtureResponse expected = buildFixtureResponse();
            mockWebServer.enqueue(jsonResponse(expected));

            ApiFixtureResponse result =
                    footballClient.getFixtures(null, null, null, null);

            assertThat(result).isNotNull();
            assertThat(result.response()).hasSizeGreaterThanOrEqualTo(1);

            String name = result.response().getFirst().league().name();
            assertThat(result.response().getFirst().league().name()).isEqualTo(name);
        }

        @Test
        void shouldSendCorrectQueryParams_whenAllParamsProvided() throws Exception {
            mockWebServer.enqueue(jsonResponse(buildFixtureResponse()));
            LocalDate now = LocalDate.now();
            Status status = Status.FT;

            footballClient.getFixtures(
                    223,
                    23,
                    now,
                    status);

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getPath())
                    .contains("league=223")
                    .contains("team=23")
                    .contains(String.format("date=%s", now))
                    .contains(String.format("status=%s", status.name().toLowerCase(Locale.ROOT)));
        }

        @Test
        void shouldOmitQueryParam_whenAllParamsExceptDateIsNull() throws Exception {
            mockWebServer.enqueue(jsonResponse(buildFixtureResponse()));

            footballClient.getFixtures(
                    null,
                    null,
                    null,
                    null);

            RecordedRequest request = mockWebServer.takeRequest();
            assertThat(request.getPath())
                    .doesNotContain("league=")
                    .doesNotContain("team=")
                    .doesNotContain("status=")
                    .contains("date");
        }
    }

    // ===========================================================================
    // 5xx error handling
    // ===========================================================================
    @Nested
    @DisplayName("5xx server errors")
    class ServerErrorTest {
        @Test
        void shouldThrowServiceUnavailable_on204() {
            mockWebServer.enqueue(emptyResponse(204));

            assertThatThrownBy(() -> footballClient.getFixtures(
                    123,
                    21,
                    LocalDate.now(),
                    Status.FT
            )).isInstanceOf(CustomServiceUnavailableException.class);
        }

        @Test
        void shouldThrowServiceUnavailable_on499() {
            mockWebServer.enqueue(emptyResponse(499));

            assertThatThrownBy(() -> footballClient.getFixtures(
                    123,
                    21,
                    LocalDate.now(),
                    Status.FT
            )).isInstanceOf(CustomServiceUnavailableException.class);
        }

        @Test
        void shouldThrowServiceUnavailable_on500() {
            mockWebServer.enqueue(emptyResponse(500));

            assertThatThrownBy(() -> footballClient.getFixtures(
                    123,
                    21,
                    LocalDate.now(),
                    Status.FT
            )).isInstanceOf(CustomServiceUnavailableException.class);
        }
    }

    // ===========================================================================
    // Empty body handling
    // ===========================================================================

    @Nested
    @DisplayName("Empty response body")
    class EmptyBodyTest {

        @Test
        void shouldThrowServiceUnavailable_whenBodyIsNull() {
            mockWebServer.enqueue(
                    new MockResponse()
                            .setResponseCode(200)
                            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody("null"));

            assertThatThrownBy(() -> footballClient.getFixtures(
                    123,
                    21,
                    LocalDate.now(),
                    Status.FT
            )).isInstanceOf(CustomServiceUnavailableException.class);
        }
    }

    // ===========================================================================
    // Connection failure handling
    // ===========================================================================

    @Nested
    @DisplayName("Connection failures")
    class ConnectionFailureTest {

        @Test
        void shouldThrowBadGatewayWhenServerIsUnreachable() throws Exception {
            mockWebServer.shutdown();

            assertThatThrownBy(() -> footballClient.getFixtures(
                    123,
                    21,
                    LocalDate.now(),
                    Status.FT
            )).isInstanceOf(CustomBadGatewayException.class);
        }
    }

}
