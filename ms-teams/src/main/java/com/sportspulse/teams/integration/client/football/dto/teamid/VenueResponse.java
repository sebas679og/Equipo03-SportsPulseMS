package com.sportspulse.teams.integration.client.football.dto.teamid;

/**
 * VenueResponse Represents the details of a sports venue. Contains information such as identity,
 * location, capacity, surface type, and image reference.
 */
public record VenueResponse(
    int id, String name, String address, String city, int capacity, String surface, String image) {}
