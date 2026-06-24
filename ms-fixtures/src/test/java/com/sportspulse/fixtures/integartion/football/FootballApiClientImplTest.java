package com.sportspulse.fixtures.integartion.football;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.fixtures.integration.football.FootballClientImpl;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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

import static org.assertj.core.api.Assertions.assertThat;

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

    private MockResponse emptyResponse(HttpStatus status) {
        return new MockResponse().setResponseCode(status.value());
    }

    @Nested
    @DisplayName("getFixtures")
    class GetFixtures{

        @Test
        void shouldReturnResponse_whenDateProvided() throws Exception{
            ApiFixtureResponse expected = buildFixtureResponse();
            mockWebServer.enqueue(jsonResponse(expected));

            ApiFixtureResponse result =
                    footballClient.getFixtures(null, null, LocalDate.now(), null);

            assertThat(result).isNotNull();
            assertThat(result.response()).hasSizeGreaterThanOrEqualTo(1);

            String name = result.response().getFirst().league().name();

            assertThat(result.response().getFirst().league().name()).isEqualTo(name);
        }
    }
}
