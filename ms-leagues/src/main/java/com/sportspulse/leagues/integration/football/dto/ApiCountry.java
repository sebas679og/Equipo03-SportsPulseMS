package com.sportspulse.leagues.integration.football.dto;

/**
 * ApiCountry Data transfer object (DTO) representing country details provided by the external
 * football API.
 *
 * <p>Contains the country's name, ISO code, and flag URL, offering essential metadata for league
 * and team classification.
 */
public record ApiCountry(String name, String code, String flag) {}
