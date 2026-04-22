package com.sportspulse.gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/** Configuration class for OpenAPI (Swagger) documentation. */
@OpenAPIDefinition(
    info =
        @Info(
            title = "SportsPulse Gateway API",
            description =
                "REST API in charge of URL redirection to services, rate limit per request, "
                    + "and system health endpoint as a whole",
            version = "1.0"))
public class OpenApiConfig {}
