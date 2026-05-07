package com.sportspulse.leagues.integrations.football.dto;

/**
 * ApiLeague Data transfer object (DTO) representing league details provided by the external
 * football API.
 *
 * <p>Contains the league's unique identifier, name, type (e.g., domestic or international), and
 * logo URL, offering essential metadata for classification and standings operations.
 */
public record ApiLeague(int id, String name, String type, String logo) {}
