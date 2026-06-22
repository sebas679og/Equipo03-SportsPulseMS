package com.sportspulse.fixtures.integration.football.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sportspulse.fixtures.utils.deserializers.FlexibleErrorsDeserializer;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFixtureResponse(
        String get,
        @JsonDeserialize(using = FlexibleErrorsDeserializer.class) List<Object> parameters,
        @JsonDeserialize(using = FlexibleErrorsDeserializer.class) List<Object> errors,
        int results,
        ApiPaging paging,
        List<ApiFixtureData> response) {}
