package com.sportspulse.leagues.integration.football.dto;

/**
 * ApiSeason Data transfer object (DTO) representing season details provided by the external
 * football API.
 *
 * <p>Contains information about the season year, start and end dates, whether the season is
 * currently active, and associated coverage details encapsulated in {@link ApiCoverage}.
 */
public record ApiSeason(
    int year, String start, String end, boolean current, ApiCoverage coverage) {}
