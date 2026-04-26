package com.sportspulse.leagues.integration.msauth.dto;

import java.util.UUID;

/** Response from ms-auth token validation endpoint. */
public record UserResponse(boolean valid, UUID userId, String username, String role) {}