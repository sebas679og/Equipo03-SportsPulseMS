package com.sportspulse.leagues.dto.responses;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * LeaguesResponse
 *
 * <p>Data transfer object (DTO) representing the response structure for league-related queries.
 * Encapsulates a collection of {@link LeagueSummary} objects, providing structured access to
 * summarized league information.
 *
 * <p>Built using Lombok annotations for immutability and the builder pattern.
 */
@Value
@Builder
public class LeaguesResponse {
  List<LeagueSummary> data;
}
