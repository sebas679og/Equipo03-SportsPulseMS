package com.sportspulse.leagues;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

/**
 * ApiFootballLeaguesStub
 *
 * <p>Utility class for stubbing API-Football league endpoints using WireMock.
 *
 * <p>Provides predefined responses for league queries by season, country, combined season and
 * country, and league ID. Facilitates integration testing by simulating external API behavior with
 * static JSON files.
 */
public class ApiFootballLeaguesStub {

  private final WireMockExtension wireMock;

  /**
   * Constructs a new stub instance bound to the given WireMock server.
   *
   * @param wireMock the WireMock server used to register stubs
   */
  public ApiFootballLeaguesStub(WireMockExtension wireMock) {
    this.wireMock = wireMock;
  }

  /**
   * Stubs the endpoint {@code GET /leagues?season=<season>} to return a predefined JSON response
   * for leagues by season.
   *
   * @param season the season year to stub
   */
  public void stubBySeason(String season) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("season", equalTo(season))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-by-season.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a predefined JSON response
   * for leagues by country.
   *
   * @param country the country name to stub
   */
  public void stubByCountry(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-by-country.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?season=<season>&country=<country>} to return a
   * predefined JSON response for leagues filtered by both season and country.
   *
   * @param season the season year to stub
   * @param country the country name to stub
   */
  public void stubBySeasonAndCountry(String season, String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("season", equalTo(season))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-by-season-and-country.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?id=<leagueId>} to return a predefined JSON response for
   * a league by its unique identifier.
   *
   * @param leagueId the league ID to stub
   */
  public void stubById(int leagueId) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("id", equalTo(String.valueOf(leagueId)))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-by-id.json")));
  }

  /**
   * Resets all registered stubs on the WireMock server.
   *
   * <p>Useful for clearing state between test executions.
   */
  public void reset() {
    wireMock.resetAll();
  }
}
