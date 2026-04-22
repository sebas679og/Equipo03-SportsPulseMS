package com.sportspulse.standings.integrations.football.dto;

/**
 * Team Record representing basic team information.
 *
 * <p>Contains the team's unique identifier, name, and logo URL. Provides essential details for
 * identifying and displaying team data within league or season contexts.
 */
public record Team(int id, String name, String logo) {}
