package com.sportspulse.standings.integrations.football.dto;

import java.util.List;

/**
 * League Record representing a football league and its standings.
 *
 * <p>Contains basic league details such as ID, name, country, logo, flag, and season year. Also
 * encapsulates a nested list of {@link ApiStanding} objects to represent grouped standings data
 * within the league context.
 */
public record ApiLeague(
    int id,
    String name,
    String country,
    String logo,
    String flag,
    int season,
    List<List<ApiStanding>> standings) {}
