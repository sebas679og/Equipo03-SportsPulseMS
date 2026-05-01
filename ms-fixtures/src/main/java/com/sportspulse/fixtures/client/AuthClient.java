package com.sportspulse.fixtures.client;

import com.sportspulse.fixtures.constants.ApiPaths;
import com.sportspulse.fixtures.constants.ErrorConstants;
import com.sportspulse.fixtures.constants.HttpHeaders;
import com.sportspulse.fixtures.dto.internal.InternalUserResponse;
import com.sportspulse.fixtures.exceptions.BadGatewayException;
import com.sportspulse.fixtures.exceptions.ServiceUnavailableException;
import com.sportspulse.fixtures.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * HTTP client responsible for communicating with the Authentication Service.
 *
 * <p>This component leverages {@link WebClient} to validate user tokens and centralizes the error
 * handling logic for specific responses from the Auth microservice.
 */
@Slf4j
@Component
public class AuthClient {

  private final WebClient authWebClient;

  public AuthClient(@Qualifier("authWebClient") WebClient authWebClient) {
    this.authWebClient = authWebClient;
  }

  private static final String NO_BODY = "No body";

  /**
   * Validates the status and authenticity of a JWT token against the Auth Service.
   *
   * @param token the access token string (excluding the "Bearer " prefix).
   * @return {@link InternalUserResponse} containing user details if the token is valid.
   * @throws UnauthorizedException if the service returns 401 (Invalid or expired token).
   * @throws BadGatewayException if the service returns 403 or an empty/null response body.
   * @throws ServiceUnavailableException if the service returns a 5xx server error.
   */
  public InternalUserResponse isTokenValid(String token) {
    return authWebClient
        .get()
        .uri(ApiPaths.Auth.VALIDATE)
        .header(HttpHeaders.Auth.AUTHORIZATION, HttpHeaders.Auth.BEARER + token)
        .retrieve()
        .onStatus(status -> status.value() == 401, this::handleUnauthorized)
        .onStatus(status -> status.value() == 403, this::handleForbidden)
        .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
        .bodyToMono(InternalUserResponse.class)
        .blockOptional()
        .orElseThrow(
            () -> {
              log.error("Auth Service returned empty or null body when validating the token");
              return new BadGatewayException(ErrorConstants.Message.SERVICE_EMPTY_OR_NULL);
            });
  }

  /**
   * Handles 401 Unauthorized responses from the Auth Service.
   *
   * @param response the {@link ClientResponse} from the server.
   * @return a {@link Mono} emitting an {@link UnauthorizedException}.
   */
  private Mono<? extends Throwable> handleUnauthorized(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .defaultIfEmpty(NO_BODY)
        .flatMap(
            body -> {
              log.warn("Auth Service 401 Unauthorized. Body: {}", body);
              return Mono.error(
                  new UnauthorizedException(ErrorConstants.Message.INVALID_OR_EXPIRED));
            });
  }

  /**
   * Handles 403 Forbidden responses. Usually indicates internal configuration issues or a rejected
   * session.
   *
   * @param response the {@link ClientResponse} from the server.
   * @return a {@link Mono} emitting a {@link BadGatewayException}.
   */
  private Mono<? extends Throwable> handleForbidden(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .defaultIfEmpty(NO_BODY)
        .flatMap(
            body -> {
              log.error("Auth Service 403 Forbidden. Internal configuration error. Body: {}", body);
              return Mono.error(new BadGatewayException(ErrorConstants.Message.SESSION_REJECTED));
            });
  }

  /**
   * Handles 5xx Server Error responses from the Auth Service.
   *
   * @param response the {@link ClientResponse} from the server.
   * @return a {@link Mono} emitting a {@link ServiceUnavailableException}.
   */
  private Mono<? extends Throwable> handleServerError(ClientResponse response) {
    return response
        .bodyToMono(String.class)
        .defaultIfEmpty(NO_BODY)
        .flatMap(
            body -> {
              log.error(
                  "Auth Service 5xx error. Status: {}, Body: {}", response.statusCode(), body);
              return Mono.error(
                  new ServiceUnavailableException(ErrorConstants.Message.SESSION_NOT_AVAILABLE));
            });
  }
}
