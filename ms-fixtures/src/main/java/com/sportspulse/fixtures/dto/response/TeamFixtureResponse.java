package com.sportspulse.fixtures.dto.response;

/**
 * Data Transfer Object (DTO) providing detailed team information within a fixture.
 *
 * <p>This record extends basic team identity by including a visual asset (logo) and the scoring
 * data. It is the standard representation for teams in non-live fixture results or schedules.
 *
 * @param id The unique identifier of the team.
 * @param name The display name of the team.
 * @param logo The URL pointing to the team's official crest or logo image.
 * @param goals The number of goals scored (null if the match hasn't started).
 */
public record TeamFixtureResponse(Long id, String name, String logo, Integer goals) {}
