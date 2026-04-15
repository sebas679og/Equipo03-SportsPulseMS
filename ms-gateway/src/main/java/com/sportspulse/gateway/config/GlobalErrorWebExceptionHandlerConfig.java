package com.sportspulse.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.gateway.exceptions.GlobalErrorWebExceptionHandler;
import java.util.List;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

/**
 * GlobalErrorWebExceptionHandlerConfig
 *
 * <p>Registers {@link GlobalErrorWebExceptionHandler} as a Spring {@code @Bean}, mirroring the
 * lifecycle used by Spring Boot's own {@code ErrorWebFluxAutoConfiguration}. This is required
 * because {@link
 * org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler}
 * validates that {@code messageWriters} and {@code messageReaders} have been set inside {@code
 * afterPropertiesSet()} — properties that can only be injected after construction via {@link
 * AbstractErrorWebExceptionHandler#setMessageWriters} and {@link
 * AbstractErrorWebExceptionHandler#setMessageReaders}.
 *
 * <p>Annotating the handler with {@code @Component} instead would bypass this wiring, causing an
 * {@code IllegalArgumentException: Property 'messageWriters' is required} at startup.
 */
@Configuration
public class GlobalErrorWebExceptionHandlerConfig {

  /**
   * Builds and fully initializes a {@link GlobalErrorWebExceptionHandler}.
   *
   * <p>View resolvers and codec writers/readers are taken from the same beans that Spring Boot's
   * auto-configuration would supply to {@link
   * org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler}.
   *
   * @param errorAttributes resolves error details from the exchange
   * @param webProperties resource properties forwarded to the parent constructor
   * @param applicationContext Spring application context
   * @param objectMapper Jackson mapper for JSON serialization
   * @param codecConfigurer provides message writers and readers for HTTP codec support
   * @param viewResolvers ordered list of view resolvers (may be empty in API-only gateways)
   * @return a fully initialized {@link GlobalErrorWebExceptionHandler}
   */
  @Bean
  public GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler(
      ErrorAttributes errorAttributes,
      WebProperties webProperties,
      ApplicationContext applicationContext,
      ObjectMapper objectMapper,
      ServerCodecConfigurer codecConfigurer,
      List<ViewResolver> viewResolvers) {

    GlobalErrorWebExceptionHandler handler =
        new GlobalErrorWebExceptionHandler(
            errorAttributes, webProperties, applicationContext, objectMapper);

    handler.setViewResolvers(viewResolvers);
    handler.setMessageWriters(codecConfigurer.getWriters());
    handler.setMessageReaders(codecConfigurer.getReaders());

    return handler;
  }
}
