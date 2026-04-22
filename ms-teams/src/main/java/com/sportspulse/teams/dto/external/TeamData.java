package com.sportspulse.teams.dto.external;

/**
 * Represents basic team information returned by the external API.
 *
 * @param id       unique identifier of the team
 * @param name     official team name
 * @param country  country where the team is based
 * @param logo     URL of the team's logo
 * @param founded  year the team was founded
 */
public record TeamData(Integer id, String name, String country, String logo, Integer founded) {}
