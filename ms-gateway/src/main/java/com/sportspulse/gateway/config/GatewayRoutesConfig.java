package com.sportspulse.gateway.config;

import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.config.constants.InternalHeaders;
import com.sportspulse.gateway.config.properties.GatewayServicesProperties;
import com.sportspulse.gateway.exceptions.GatewayFallbackHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/** GatewayRoutesConfig Configures gateway routes and fallback handlers. */
@Configuration
@RequiredArgsConstructor
public class GatewayRoutesConfig {

  private final GatewayServicesProperties services;
  private final GatewayRouteFactory routeFactory;

  /**
   * Defines the main gateway routes for authentication services.
   *
   * @param builder the RouteLocatorBuilder used to construct routes
   * @return a RouteLocator with configured routes
   */
  @Bean
  public RouteLocator routes(RouteLocatorBuilder builder) {
    return builder
        .routes()
        .route(
            "ms-auth-login",
            r ->
                r.path(ApiPathsServices.Auth.LOGIN)
                    .filters(routeFactory.applyBruteForceFilters("ms-auth"))
                    .uri(services.getAuth()))
        .route(
            "ms-auth",
            r ->
                r.path(ApiPathsServices.Auth.ALL)
                    .filters(routeFactory.applyStandardFilters("ms-auth"))
                    .uri(services.getAuth()))
        .build();
  }

  /**
   * Defines fallback routes for handling service unavailability.
   *
   * @param handler the GatewayFallbackHandler to process fallback requests
   * @return a RouterFunction mapping fallback paths to handlers
   */
  @Bean
  public RouterFunction<ServerResponse> fallbackRoutes(GatewayFallbackHandler handler) {
    return RouterFunctions.route()
        .route(RequestPredicates.path("/fallback/503"), handler::serviceUnavailable)
        .build();
  }
}
