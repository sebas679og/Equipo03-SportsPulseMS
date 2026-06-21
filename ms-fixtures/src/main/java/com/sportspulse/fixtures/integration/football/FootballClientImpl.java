package com.sportspulse.fixtures.integration.football;

import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomServiceUnavailableException;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FootballClientImpl implements FootballClient{

    private ApiFixtureResponse executeRequest(
            WebClient.RequestHeadersSpec<?> request, String standingKey) {
        return request
                .retrieve()
                .onStatus(
                        status -> status.value() == 204,
                        response ->
                                response
                                        .bodyToMono(String.class)
                                        .defaultIfEmpty("No body")
                                        .flatMap(
                                                body -> {
                                                    log.error(
                                                            "Api-Football 204 No Content. "
                                                                    + "The requested standing Api-Football error. "
                                                                    + "fixtures: {}, Body: {}",
                                                            standingKey,
                                                            response);
                                                    return Mono.error(
                                                            new CustomServiceUnavailableException(
                                                                    "Api-Football is not currently available, please try again"));
                                                }))
                .onStatus(
                        status -> status.value() == 499 || status.value() == 500,
                        response ->
                                response
                                        .bodyToMono(String.class)
                                        .defaultIfEmpty("No body")
                                        .flatMap(
                                                body -> {
                                                    if (log.isErrorEnabled()) {
                                                        log.error(
                                                                "Api-Football error. Status: {}, Body: {}",
                                                                response.statusCode(),
                                                                body);
                                                    }
                                                    return Mono.error(
                                                            new CustomServiceUnavailableException(
                                                                    "Api-Football is not currently available, please try again"));
                                                }))
                .bodyToMono(ApiFixtureResponse.class)
                .onErrorMap(
                        WebClientRequestException.class,
                        ex -> {
                            if (log.isErrorEnabled()) {
                                log.error(
                                        "Api-Football is unreachable. Cause: {} - {}",
                                        ex.getCause() != null ? ex.getCause().getCause().getMessage() : ex.getClass().getSimpleName(),
                                        ex.getMessage());
                            }
                            return new CustomBadGatewayException("Session validation service is unreachable");
                        })
                .blockOptional()
                .orElseThrow(
                        () -> {
                            log.error(
                                    "Api-Football returned an empty or null body for the requested. standing: {}",
                                    standingKey);
                            return new CustomServiceUnavailableException(
                                    "Api-Football is not currently available, please try again");
                        });
    }
}
