package com.sportspulse.teams.integration.msauth.dto;

import java.util.UUID;

/**
 * UserResponse Represents the response structure for user validation and details. Contains
 * information about the validity of the user, unique identifier, username, and assigned role.
 */
public record UserResponse(boolean valid, UUID userId, String username, String role) {}
