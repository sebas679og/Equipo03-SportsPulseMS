package com.sportspulse.teams.config;

import com.sportspulse.teams.constants.HttpHeaders;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Feign client configuration for internal service-to-service communication.
 *
 * <p>Registers a {@link RequestInterceptor} that automatically injects the
 * internal API key header into every outgoing Feign request.
 */
public class FeignConfig {

    @Value("${app.security.internal-api-key}")
    private String internalApiKey;

    /**
     * Creates a {@link RequestInterceptor} that adds the internal API key header.
     *
     * @return a RequestInterceptor that injects {@value HttpHeaders#X_INTERNAL_API_KEY}
     *         into every Feign request.
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate ->
            requestTemplate.header(HttpHeaders.X_INTERNAL_API_KEY, internalApiKey);
    }
}
