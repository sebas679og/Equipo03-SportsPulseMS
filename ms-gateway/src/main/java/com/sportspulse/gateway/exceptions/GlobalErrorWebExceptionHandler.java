package com.sportspulse.gateway.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.gateway.config.GlobalErrorWebExceptionHandlerConfig;
import com.sportspulse.gateway.dto.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

/**
 * GlobalErrorWebExceptionHandler
 *
 * <p>Replaces Spring Boot's {@link DefaultErrorWebExceptionHandler} (registered at
 * {@code @Order(-1)}) by running at {@code @Order(-2)}, giving it higher priority. Every unhandled
 * error — including 404 Not Found for unknown routes — is serialized as a JSON {@link
 * ErrorResponse} instead of the default Spring Boot error body.
 *
 * <p>This class must <strong>not</strong> be annotated with {@code @Component}. It is registered as
 * a {@code @Bean} inside {@link GlobalErrorWebExceptionHandlerConfig} so that Spring Boot can
 * inject the required {@code ServerCodecConfigurer} message writers/readers before {@code
 * afterPropertiesSet()} is called — exactly the same lifecycle that {@code
 * ErrorWebFluxAutoConfiguration} follows for {@link DefaultErrorWebExceptionHandler}.
 */
@Slf4j
@Order(-2)
public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

  private final ObjectMapper objectMapper;

  /**
   * Constructs the handler.
   *
   * @param errorAttributes provides the error details extracted from the exchange
   * @param webProperties web resource properties (used by the parent handler)
   * @param applicationContext Spring application context
   * @param objectMapper Jackson mapper used to serialize {@link ErrorResponse}
   */
  public GlobalErrorWebExceptionHandler(
      ErrorAttributes errorAttributes,
      WebProperties webProperties,
      ApplicationContext applicationContext,
      ObjectMapper objectMapper) {
    super(
        errorAttributes,
        webProperties.getResources(),
        new org.springframework.boot.autoconfigure.web.ErrorProperties(),
        applicationContext);
    this.objectMapper = objectMapper;
  }

  /**
   * Intercepts every error request and writes a standardized JSON {@link ErrorResponse}. Falls back
   * to an empty response if serialization fails.
   *
   * @param request the incoming server request that triggered the error
   * @return a {@link Mono} that completes when the JSON body has been written to the response
   */
  @Override
  protected Mono<org.springframework.web.reactive.function.server.ServerResponse>
      renderErrorResponse(ServerRequest request) {
    int statusCode =
        resolveStatusCode(
            getErrorAttributes(
                request, org.springframework.boot.web.error.ErrorAttributeOptions.defaults()));
    HttpStatus status = HttpStatus.resolve(statusCode);
    if (status == null) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    HttpStatus finalStatus = status;
    Throwable error = getError(request);

    if (log.isErrorEnabled()) {
      log.error(
          "Unhandled error — status={} path='{}' message='{}'",
          finalStatus.value(),
          request.path(),
          error.getMessage());
    }

    ServerHttpResponse response = request.exchange().getResponse();
    response.setStatusCode(finalStatus);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    try {
      byte[] body =
          objectMapper.writeValueAsBytes(
              ErrorResponse.builder()
                  .code(finalStatus.value())
                  .name(finalStatus.name())
                  .description(resolveDescription(finalStatus, error))
                  .build());
      DataBuffer buffer = response.bufferFactory().wrap(body);
      return response.writeWith(Mono.just(buffer)).then(Mono.empty());
    } catch (JsonProcessingException ex) {
      if (log.isErrorEnabled()) {
        log.error("Serialization failed: {}", ex.getMessage());
      }
      return response.setComplete().then(Mono.empty());
    }
  }

  /**
   * Produces a clean, human-readable description for the error response.
   *
   * <p>Spring's {@link org.springframework.web.server.ResponseStatusException} messages contain the
   * raw reason phrase plus the exception detail (e.g. {@code "404 NOT_FOUND \"No static resource
   * api/foo.\""}). We strip that noise and return only a concise sentence derived from the HTTP
   * status, keeping the raw message only for unexpected server-side errors where the detail is
   * actually useful.
   *
   * @param status the resolved HTTP status
   * @param error the underlying throwable
   * @return a clean description string
   */
  protected String resolveDescription(HttpStatus status, Throwable error) {
    return switch (status) {
      case NOT_FOUND -> "The requested resource was not found.";
      case METHOD_NOT_ALLOWED -> "HTTP method not allowed for this endpoint.";
      case UNAUTHORIZED -> "Authentication is required to access this resource.";
      case FORBIDDEN -> "You do not have permission to access this resource.";
      case TOO_MANY_REQUESTS -> "Too many requests, please try again later.";
      case BAD_REQUEST -> "The request is malformed or contains invalid parameters.";
      case SERVICE_UNAVAILABLE -> "The service is temporarily unavailable. Please try again later.";
      case GATEWAY_TIMEOUT -> "The upstream service did not respond in time.";
      default ->
          status.is5xxServerError() && error.getMessage() != null
              ? error.getMessage()
              : status.getReasonPhrase();
    };
  }

  /**
   * Extracts the HTTP status code from the error attributes map. Named {@code resolveStatusCode} to
   * avoid clashing with the {@code protected} {@code getHttpStatus(Map)} method declared in the
   * parent class.
   *
   * @param errorAttributes the map produced by {@link ErrorAttributes}
   * @return the resolved HTTP status code, or 500 if not determinable
   */
  protected int resolveStatusCode(java.util.Map<String, Object> errorAttributes) {
    Object statusAttr = errorAttributes.get("status");
    if (statusAttr instanceof Integer code) {
      return code;
    }
    try {
      return Integer.parseInt(String.valueOf(statusAttr));
    } catch (NumberFormatException e) {
      return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
  }
}
