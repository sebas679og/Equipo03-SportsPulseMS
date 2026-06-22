package com.sportspulse.fixtures.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFixtureResponse(
        String get,
        JsonNode parameters,
        JsonNode errors,
        int results,
        ApiPaging paging,
        List<ApiFixtureData> response) {}
