package com.sportspulse.fixtures.integration.msauth;

import com.sportspulse.fixtures.config.constants.ApiPaths;
import com.sportspulse.fixtures.config.constants.InternalHeaders;
import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomServiceUnavailableException;
import com.sportspulse.fixtures.exceptions.CustomUnauthorizedException;
import com.sportspulse.fixtures.integration.msauth.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthClientImpl implements AuthClient{

    private final WebClient authWebClient;

    public AuthClientImpl(@Qualifier("msAuthWebClient") WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    @Override
    public UserResponse isTokenValid(String token) {
        WebClient.RequestHeadersSpec<?> request =
                authWebClient
                        .get()
                        .uri(ApiPaths.AuthService.VALIDATE_TOKEN)
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                String.join(" ", InternalHeaders.MsAuth.TYPE_TOKEN, token));

        return executeRequest(request);
    }

    private UserResponse executeRequest(WebClient.RequestHeadersSpec<?> request) {
        return request
                .retrieve()
                .onStatus(
                        status -> status.value() == 401,
                        response ->
                                response
                                        .bodyToMono(String.class)
                                        .defaultIfEmpty("No body")
                                        .flatMap(
                                                body -> {
                                                    log.warn("Auth Service 401 Unauthorized. Body: {}", body);
                                                    return Mono.error(
                                                            new CustomUnauthorizedException(
                                                                    "Invalid or expired token, please log in again"));
                                                }))
                .onStatus(
                        status -> status.value() == 403,
                        response ->
                                response
                                        .bodyToMono(String.class)
                                        .defaultIfEmpty("No body")
                                        .flatMap(
                                                body -> {
                                                    log.error(
                                                            "Auth Service 403 Forbidden. Internal configuration error of the "
                                                                    + "integration. Body: {}",
                                                            body);
                                                    return Mono.error(
                                                            new CustomBadGatewayException(
                                                                    "Session validation service rejected the request"));
                                                }))
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        response ->
                                response
                                        .bodyToMono(String.class)
                                        .defaultIfEmpty("No body")
                                        .flatMap(
                                                body -> {
                                                    if (log.isErrorEnabled()) {
                                                        log.error(
                                                                "Auth Service 5xx error. Status: {}, Body: {}",
                                                                response.statusCode(),
                                                                body);
                                                    }
                                                    return Mono.error(
                                                            new CustomServiceUnavailableException(
                                                                    "Session validation service is not available at this time"));
                                                }))
                .bodyToMono(UserResponse.class)
                .onErrorMap(
                        WebClientRequestException.class,
                        ex -> {
                            if (log.isErrorEnabled()) {
                                log.error(
                                        "Auth Service is unreachable. Cause: {} - {}",
                                        ex.getClass().getSimpleName(),
                                        ex.getMessage());
                            }
                            return new CustomBadGatewayException("Session validation service is unreachable");
                        })
                .blockOptional()
                .orElseThrow(
                        () -> {
                            log.error("Auth Service returned empty or null body when validating the token");
                            return new CustomServiceUnavailableException(
                                    "Session validation service is not available at this time");
                        });
    }
}
