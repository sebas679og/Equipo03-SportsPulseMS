package com.sportspulse.standings.integrations.football.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sportspulse.standings.utils.deserializers.FlexibleErrorsDeserializer;
import java.util.List;

/**
 * StandingsResponse Record representing the API response for league standings.
 *
 * <p>Contains metadata about the request, including the endpoint called, query parameters, errors,
 * result count, and pagination details. Also encapsulates a list of {@link ApiResponse} objects
 * providing structured access to league and standings information.
 */
public record ApiStandingsResponse(
    String get,
    ApiParameters parameters,
    @JsonDeserialize(using = FlexibleErrorsDeserializer.class) List<Object> errors,
    int results,
    ApiPaging paging,
    List<ApiResponse> response) {}
