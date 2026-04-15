package com.sportspulse.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sportspulse.gateway.config.properties.GatewayServicesProperties;
import com.sportspulse.gateway.exceptions.GatewayFallbackHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("GatewayRoutesConfig Tests")
class GatewayRoutesConfigTest {

  @Mock private GatewayServicesProperties services;
  @Mock private GatewayRouteFactory routeFactory;
  @Mock private GatewayFallbackHandler fallbackHandler;

  private GatewayRoutesConfig gatewayRoutesConfig;

  @BeforeEach
  void setUp() {
    gatewayRoutesConfig = new GatewayRoutesConfig(services, routeFactory);
  }

  // ─── routes() ────────────────────────────────────────────────────────────

  @Test
  @DisplayName("routes() returns a non-null RouteLocator")
  void routes_returnsNonNullRouteLocator() {
    RouteLocator result = gatewayRoutesConfig.routes(deepStubBuilder());
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("routes() registers exactly two routes: ms-auth-login and ms-auth")
  void routes_registersBothRouteIds() {
    List<String> registeredIds = new ArrayList<>();
    RouteLocatorBuilder builder = builderCapturingRouteIds(registeredIds);

    gatewayRoutesConfig.routes(builder);

    assertThat(registeredIds).containsExactly("ms-auth-login", "ms-auth");
  }

  @Test
  @DisplayName(
      "routes() applies brute-force filters to ms-auth-login and standard filters to ms-auth")
  void routes_appliesCorrectFiltersToEachRoute() {
    List<String> registeredIds = new ArrayList<>();
    final RouteLocatorBuilder builder = builderCapturingRouteIds(registeredIds);

    // Provide real-enough stubs so the lambdas inside route() can execute
    @SuppressWarnings("unchecked")
    Function<GatewayFilterSpec, UriSpec> bruteForce = mock(Function.class);
    @SuppressWarnings("unchecked")
    Function<GatewayFilterSpec, UriSpec> standard = mock(Function.class);
    when(routeFactory.applyBruteForceFilters("ms-auth")).thenReturn(bruteForce);
    when(routeFactory.applyStandardFilters("ms-auth")).thenReturn(standard);
    when(services.getAuth()).thenReturn("http://ms-auth:8080");

    gatewayRoutesConfig.routes(builder);

    verify(routeFactory).applyBruteForceFilters("ms-auth");
    verify(routeFactory).applyStandardFilters("ms-auth");
  }

  @Test
  @DisplayName("routes() reads the auth service URI from GatewayServicesProperties")
  void routes_usesAuthUriFromProperties() {
    List<String> registeredIds = new ArrayList<>();
    final RouteLocatorBuilder builder = builderCapturingRouteIds(registeredIds);

    @SuppressWarnings("unchecked")
    Function<GatewayFilterSpec, UriSpec> bruteForce = mock(Function.class);
    @SuppressWarnings("unchecked")
    Function<GatewayFilterSpec, UriSpec> standard = mock(Function.class);
    when(routeFactory.applyBruteForceFilters("ms-auth")).thenReturn(bruteForce);
    when(routeFactory.applyStandardFilters("ms-auth")).thenReturn(standard);
    when(services.getAuth()).thenReturn("http://ms-auth:8080");

    gatewayRoutesConfig.routes(builder);

    // getAuth() is called once per route definition (two routes share the same URI)
    verify(services, times(2)).getAuth();
  }

  // ─── fallbackRoutes() ─────────────────────────────────────────────────────

  @Test
  @DisplayName("fallbackRoutes() returns a non-null RouterFunction")
  void fallbackRoutes_returnsNonNullRouterFunction() {
    RouterFunction<ServerResponse> result = gatewayRoutesConfig.fallbackRoutes(fallbackHandler);
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("fallbackRoutes() builds the router without invoking the handler eagerly")
  void fallbackRoutes_doesNotInvokeHandlerEagerly() {
    gatewayRoutesConfig.fallbackRoutes(fallbackHandler);
    verifyNoInteractions(fallbackHandler);
  }

  // ─── helpers ──────────────────────────────────────────────────────────────

  /**
   * Returns a {@link RouteLocatorBuilder} whose entire fluent chain is satisfied by {@code
   * RETURNS_DEEP_STUBS}. Use this when only the final {@link RouteLocator} result matters and the
   * inner route-definition lambdas do <em>not</em> need to be executed.
   */
  private RouteLocatorBuilder deepStubBuilder() {
    return mock(RouteLocatorBuilder.class, RETURNS_DEEP_STUBS);
  }

  /**
   * Returns a {@link RouteLocatorBuilder} whose {@code routes().route(id, fn)} call
   * <em>executes</em> the route-definition lambda {@code fn} so that collaborators ({@link
   * GatewayRouteFactory}, {@link GatewayServicesProperties}) are actually invoked. The {@code id}
   * of every registered route is collected into {@code capturedIds}.
   *
   * <p>The {@link PredicateSpec} passed to each lambda is a deep-stub mock so that the full {@code
   * r.path(...).filters(...).uri(...)} chain never throws NPE.
   */
  private RouteLocatorBuilder builderCapturingRouteIds(List<String> capturedIds) {
    RouteLocatorBuilder builder = mock(RouteLocatorBuilder.class, RETURNS_DEEP_STUBS);
    RouteLocatorBuilder.Builder routesBuilder =
        mock(RouteLocatorBuilder.Builder.class, RETURNS_DEEP_STUBS);

    when(builder.routes()).thenReturn(routesBuilder);

    // Execute every route-definition lambda so inner collaborators are reached
    doAnswer(
            invocation -> {
              capturedIds.add(invocation.getArgument(0));
              Function<PredicateSpec, UriSpec> fn = invocation.getArgument(1);
              PredicateSpec predicateSpec = mock(PredicateSpec.class, RETURNS_DEEP_STUBS);
              fn.apply(predicateSpec); // runs r.path(...).filters(...).uri(...)
              return routesBuilder;
            })
        .when(routesBuilder)
        .route(anyString(), any());

    return builder;
  }
}
