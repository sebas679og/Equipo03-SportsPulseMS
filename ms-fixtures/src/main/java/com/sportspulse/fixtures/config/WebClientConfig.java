package com.sportspulse.fixtures.config;

import com.sportspulse.fixtures.config.constants.InternalHeaders;
import com.sportspulse.fixtures.config.properties.ApiFootballProperties;
import com.sportspulse.fixtures.config.properties.AuthProperties;
import com.sportspulse.fixtures.config.properties.WebClientTimeoutProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

/**
 * WebClientConfig Provides configuration for {@link WebClient} instances used to interact with
 * external services such as the Football API and the internal authentication service. Each client
 * is preconfigured with its base URL and required authentication headers.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a {@link WebClient} instance for the Football API. Configures the client with the base
     * URL and attaches the API key as a request header for authentication.
     *
     * @param apiFootballProperties the configuration properties containing the Football API base URL
     *     and API key
     * @return a configured {@link WebClient} for the Football API
     */
    @Bean("apiFootballWebClient")
    public WebClient apiFootballWebClient(ApiFootballProperties apiFootballProperties) {
        return baseBuilder(apiFootballProperties)
                .filter(
                        (request, next) -> {
                            ClientRequest newRequest =
                                    ClientRequest.from(request)
                                            .header(
                                                    InternalHeaders.ApiFootball.API_FOOTBALL_KEY,
                                                    apiFootballProperties.getApiKey())
                                            .build();
                            return next.exchange(newRequest);
                        })
                .build();
    }

    /**
     * Creates a {@link WebClient} instance for the internal authentication service. Configures the
     * client with the base URL and attaches the internal API key as a request header for secure
     * communication between microservices.
     *
     * @param msAuthProperties the configuration properties containing the authentication service base
     *     URL and API key
     * @return a configured {@link WebClient} for the authentication service
     */
    @Bean("msAuthWebClient")
    public WebClient msAuthWebClient(AuthProperties msAuthProperties) {
        return baseBuilder(msAuthProperties)
                .filter(
                        (request, next) -> {
                            ClientRequest newRequest =
                                    ClientRequest.from(request)
                                            .header(InternalHeaders.MsAuth.MS_AUTH_KEY, msAuthProperties.getApiKey())
                                            .build();
                            return next.exchange(newRequest);
                        })
                .build();
    }

    /**
     * Builds a {@link WebClient.Builder} pre-configured with base URL, connection/read/write
     * timeouts, and an in-memory buffer limit. Callers must add their own auth filter and call
     * {@code .build()}.
     */
    private WebClient.Builder baseBuilder(WebClientTimeoutProperties properties) {
        HttpClient httpClient =
                HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getConnectTimeoutMs())
                        .doOnConnected(
                                conn ->
                                        conn.addHandlerLast(
                                                        new ReadTimeoutHandler(
                                                                properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                properties.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)));

        ExchangeStrategies strategies =
                ExchangeStrategies.builder()
                        .codecs(
                                config ->
                                        config
                                                .defaultCodecs()
                                                .maxInMemorySize(properties.getMemorySize() * 1024 * 1024))
                        .build();

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
    }
}
