package com.sportspulse.teams.dto.internal;

import lombok.Builder;

/**
 * Represents the result of a token validation process.
 *
 * @param valid indicates whether the token is valid
 * @param userId unique identifier of the authenticated user
 * @param username username associated with the token
 * @param role role assigned to the authenticated user
 */
@Builder
public record ValidateResponse(Boolean valid, String userId, String username, String role) {}
