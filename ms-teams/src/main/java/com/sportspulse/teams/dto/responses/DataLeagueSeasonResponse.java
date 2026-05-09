package com.sportspulse.teams.dto.responses;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * DataLeagueSeasonResponse Data transfer object (DTO) representing a collection of teams
 * participating in a league season.
 *
 * <p>Encapsulates a list of {@link TeamLeagueSeasonResponse} objects, providing structured access
 * to team information within the context of a specific league season.
 */
@Value
@Builder
public class DataLeagueSeasonResponse {
  List<TeamLeagueSeasonResponse> data;
}
