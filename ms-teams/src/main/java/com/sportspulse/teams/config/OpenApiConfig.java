package com.sportspulse.teams.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * OpenAPI/Swagger global configuration for the Sports Pulse Teams API.
 *
 * <p>Defines the API metadata and the Bearer token security scheme
 * used to authenticate requests via JWT.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "SportsPulse Teams API",
                description = "REST API for querying football teams by league and season.",
                version = "1.0"))
@SecurityScheme(
        name = "BearerAuth",
        description = "JWT Authorization header using Bearer scheme",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {}
