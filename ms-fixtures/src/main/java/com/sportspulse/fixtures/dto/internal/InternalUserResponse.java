package com.sportspulse.fixtures.dto.internal;

import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing an internal user identity within the system.
 *
 * <p>This record is typically used for inter-service communication to pass authenticated user
 * details, roles, and status after a successful authentication or validation check.
 *
 * @param userId The unique UUID assigned to the user.
 * @param username The unique login or display name of the user.
 * @param role The security role or authority assigned to the user (e.g., "ADMIN", "USER").
 * @param valid A flag indicating if the user's session or account is currently active and
 *     authorized.
 */
public record InternalUserResponse(UUID userId, String username, String role, boolean valid) {}
