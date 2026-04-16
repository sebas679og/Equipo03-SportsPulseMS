package com.sportspulse.teams.integration.client.football.dto.teamid;

/**
 * TeamResponse Represents the details of a football team. Contains information such as identity,
 * name, code, country, foundation year, national team flag, and logo reference.
 */
public record TeamResponse(
    int id, String name, String code, String country, int founded, boolean national, String logo) {}
