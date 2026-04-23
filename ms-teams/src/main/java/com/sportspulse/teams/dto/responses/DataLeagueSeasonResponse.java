package com.sportspulse.teams.dto.responses;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * DataLeagueSeasonResponse Data transfer object (DTO) representing a collection of teams
 * participating in a league season.
 *
 * <p>Encapsulates a list of {@link TeamLeagueSeasonResponse} objects, providing structured access
 * to team information within the context of a specific league season.
 */
@Data
@Builder
public class DataLeagueSeasonResponse {
  private List<TeamLeagueSeasonResponse> data;
}
