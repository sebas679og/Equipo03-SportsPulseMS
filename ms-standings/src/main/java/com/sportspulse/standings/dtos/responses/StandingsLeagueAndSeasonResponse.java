package com.sportspulse.standings.dtos.responses;

import com.sportspulse.standings.dtos.responses.complements.League;
import com.sportspulse.standings.dtos.responses.complements.Standing;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * StandingsLeagueAndSeasonResponse Data transfer object (DTO) representing league standings for a
 * specific season.
 *
 * <p>Encapsulates the {@link League} details and a list of {@link Standing} objects providing
 * structured information about team rankings and statistics within the given league and season
 * context.
 *
 * <p>Built using Lombok annotations {@link Value} and {@link Builder} to ensure immutability and
 * provide a fluent builder pattern.
 */
@Value
@Builder
public class StandingsLeagueAndSeasonResponse {
  League league;
  List<Standing> standings;
}
