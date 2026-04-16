package com.sportspulse.teams.integration.dto.response.teamid;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

/**
 * TeamResponse
 * Represents the details of a football team.
 * Contains information such as identity, name, code, country,
 * foundation year, national team flag, and logo reference.
 */
@Value
@Getter
@Builder
public class TeamResponse {
    int id;
    String name;
    String code;
    String country;
    int founded;
    boolean national;
    String logo;
}

