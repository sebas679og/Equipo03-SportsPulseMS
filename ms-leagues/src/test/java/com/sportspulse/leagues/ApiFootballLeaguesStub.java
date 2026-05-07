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
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a 204 No Content response
   * with a predefined JSON body.
   *
   * @param country the country name to stub
   */
  public void stubByCountry_Return204(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(204)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-response-204.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a 499 Client Closed Request
   * response with a predefined error JSON body.
   *
   * @param country the country name to stub
   */
  public void stubByCountry_Return499(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(499)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-response-error.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a 500 Internal Server Error
   * response with a predefined error JSON body.
   *
   * @param country the country name to stub
   */
  public void stubByCountry_Return500(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(500)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-response-error.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a 200 Internal Server Error
   * response with an empty JSON array body.
   *
   * @param country the country name to stub
   */
  public void stubByCountry_ReturnEmpty(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a 200 OK response with a
   * predefined JSON body simulating a "plan error".
   *
   * @param country the country name to stub
   */
  public void stubByCountry_ReturnErrorPlan(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-response-error-plan.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a 200 OK response with a
   * predefined JSON body simulating a "too many requests" error.
   *
   * @param country the country name to stub
   */
  public void stubByCountry_ReturnErrorRequests(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-response-error-requests.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a 200 OK response with a
   * predefined JSON body simulating multiple error messages.
   *
   * @param country the country name to stub
   */
  public void stubByCountry_ReturnErrors(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-response-errors.json")));
  }

  /**
   * Stubs the endpoint {@code GET /leagues?country=<country>} to return a 200 OK response with a
   * predefined JSON body simulating an empty result.
   *
   * <p>Used to represent scenarios where no leagues are found for the given country, returning a
   * "not found" JSON structure.
   *
   * @param country the country name to stub
   */
  public void stubByCountry_ReturnResponseEmpty(String country) {
    wireMock.stubFor(
        get(urlPathEqualTo("/leagues"))
            .withQueryParam("country", equalTo(country))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-football/leagues/leagues-by-country-not-found.json")));
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
