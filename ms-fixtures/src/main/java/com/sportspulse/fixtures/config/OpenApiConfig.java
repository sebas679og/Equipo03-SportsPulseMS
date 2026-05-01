package com.sportspulse.fixtures.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 *
 * <p>This class defines the global metadata for the API documentation, including the title,
 * version, and description. It also configures the security requirements needed to access protected
 * endpoints via the Swagger UI.
 *
 * @author Sportspulse Team
 * @version 1.0
 */
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
public class OpenApiConfig {}
