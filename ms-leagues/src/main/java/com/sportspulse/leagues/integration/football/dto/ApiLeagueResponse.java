package com.sportspulse.leagues.integration.football.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sportspulse.leagues.utils.deserializers.FlexibleErrorsDeserializer;

import java.util.List;

/**
 * ApiLeagueResponse Data transfer object (DTO) representing the response structure for
 * league-related queries provided by the external football API.
 *
 * <p>Encapsulates metadata about the request, including the request type, parameters, potential
 * errors, result count, pagination details, and a list of {@link ApiResponse} objects containing
 * league, country, and season information.
 */
public record ApiLeagueResponse(
    String get,
    @JsonDeserialize(using = FlexibleErrorsDeserializer.class) List<Object> parameters,
    @JsonDeserialize(using = FlexibleErrorsDeserializer.class) List<Object> errors,
    int results,
    ApiPaging paging,
    List<ApiResponse> response) {}
