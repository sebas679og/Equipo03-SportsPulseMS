package com.sportspulse.fixtures.integration.football;

import com.sportspulse.fixtures.config.constants.CacheConstants;
import com.sportspulse.fixtures.exceptions.CustomBadGatewayException;
import com.sportspulse.fixtures.exceptions.CustomServiceUnavailableException;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureResponse;
import com.sportspulse.fixtures.utils.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class FootballClientImpl implements FootballClient{

    private final WebClient apiFootballWebClient;

    public FootballClientImpl(@Qualifier("apiFootballWebClient") WebClient apiFootballWebClient) {
        this.apiFootballWebClient = apiFootballWebClient;
    }


    @Override
    @Cacheable(value = CacheConstants.FIXTURES_CACHE, key = "#league + '-' + #team + '-' + #date + '-' + #status + '-' + #season")
    public ApiFixtureResponse getFixtures(Integer league, Integer team, LocalDate date, Status status, Integer season) {
        final AtomicReference<URI> uriTracker = new AtomicReference<>();
        WebClient.RequestHeadersSpec<?> request =
                apiFootballWebClient.get()
                        .uri(
                                uriBuilder -> {
                                    uriBuilder.path("/fixtures");
                                    uriBuilder.queryParam("date", date);
                                    if (league != null){
                                        uriBuilder.queryParam("league", league);
                                    }
                                    if (team != null){
                                        uriBuilder.queryParam("team", team);
                                    }
                                    if (status != null){
                                        uriBuilder.queryParam("status", status.name().toLowerCase(Locale.ROOT));
                                    }
                                    if (season != null){
                                        uriBuilder.queryParam("season", season);
                                    }
                                    URI builtUri = uriBuilder.build();
                                    uriTracker.set(builtUri);
                                    return builtUri;
                                });
        String fullUrlPath =
                uriTracker.get() != null ? uriTracker.get().toString() : "/fixtures";
        return executeRequest(request, fullUrlPath);
    }

    private ApiFixtureResponse executeRequest(
            WebClient.RequestHeadersSpec<?> request, String fixtureKey) {
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
                                                            fixtureKey,
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
                                    fixtureKey);
                            return new CustomServiceUnavailableException(
                                    "Api-Football is not currently available, please try again");
                        });
    }
}
