package com.sportspulse.teams.client;

import com.sportspulse.teams.config.FeignConfig;
import com.sportspulse.teams.constants.ApiPaths;
import com.sportspulse.teams.constants.HttpHeaders;
import com.sportspulse.teams.dto.internal.ValidateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for communicating with the authentication microservice.
 * Responsible for delegating JWT token validation and role retrieval.
 */
@FeignClient(
        name = "${auth.service.name}",
        url = "${auth.service.url}",
        configuration = FeignConfig.class
)
public interface AuthClient {
    /**
     * Validates a JWT token against the identity service.
     *
     * @param token The authorization token in ‘Bearer {jwt}’ format.
     * @return ValidateResponse containing the validation status, username and roles.
     */
    @PostMapping(ApiPaths.Auth.VALIDATE)
    ValidateResponse validate(@RequestHeader(HttpHeaders.AUTHORIZATION) String token);
}
