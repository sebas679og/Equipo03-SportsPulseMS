package com.sportspulse.teams.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the response returned by the external API-Football service.
 * <p>
 * The API wraps the actual team and venue information inside a "response" JSON field,
 * which is mapped to the {@code teams} property.
 *
 * @param teams list of team and venue wrapper objects returned by the external API
 */
public record ApiFootballResponse(@JsonProperty("response") List<TeamVenueWrapper> teams) {}
