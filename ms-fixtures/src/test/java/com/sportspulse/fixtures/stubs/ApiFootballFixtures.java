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
                                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile(
                                                "api-football/fixtures/fixtures-by-date.json")));
    }

    public void stubByErrorPlan(String date){
        wireMock.stubFor(
                get(urlPathEqualTo("/fixtures"))
                        .withQueryParam("date", equalTo(date))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile(
                                                "api-football/fixtures/fixtures-by-error-plan-date.json")));
    }

    public void stubByLeagueAndSeason(String league, String season){
        wireMock.stubFor(
                get(urlPathEqualTo("/fixtures"))
                        .withQueryParam("league", equalTo(league))
                        .withQueryParam("season", equalTo(season))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile(
                                                "api-football/fixtures/fixtures-by-league-and-season.json")));
    }

    public void stubByStatusSeasonAndLeague(String status, String season, String league){
        wireMock.stubFor(
                get(urlPathEqualTo("/fixtures"))
                        .withQueryParam("status", equalTo(status))
                        .withQueryParam("league", equalTo(league))
                        .withQueryParam("season", equalTo(season))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile(
                                                "api-football/fixtures/fixtures-by-status-season-and-league.json")));
    }

    public void reset(){wireMock.resetAll();}
}
