package com.sportspulse.fixtures.stubs;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class ApiFootballFixtures {

    private final WireMockExtension wireMock;

    public ApiFootballFixtures(WireMockExtension wireMock){this.wireMock = wireMock;}

    public void stubByDate(String date){
        wireMock.stubFor(
                get(urlPathEqualTo("/fixtures"))
                        .withQueryParam("date", equalTo(date))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("api-football/fixtures/fixtures-by-date.json")));
    }

    public void reset(){wireMock.resetAll();}
}
