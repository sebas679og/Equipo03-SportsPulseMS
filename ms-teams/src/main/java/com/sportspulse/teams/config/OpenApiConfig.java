package com.sportspulse.teams.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/** Configuration class for OpenAPI (Swagger) documentation. */
@OpenAPIDefinition(
    info =
        @Info(
            title = "SportsPulse Teams API",
            description = "REST API for querying teams to api-football-teams",
            version = "1.0"))
@SecurityScheme(
    name = "BearerAuth",
    description = "JWT Authorization header using Bearer scheme",
    type = SecuritySchemeType.HTTP,
    scheme = "Bearer",
    bearerFormat = "JWT")
@SecurityScheme(
    name = "InternalApiKey",
    description = "Internal API Key for service-to-service communication",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = "X-Internal-API-Key")
public class OpenApiConfig {}
