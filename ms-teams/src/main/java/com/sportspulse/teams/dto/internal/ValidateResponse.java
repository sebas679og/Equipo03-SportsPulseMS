package com.sportspulse.teams.dto.internal;

import lombok.Builder;

@Builder
public record ValidateResponse(Boolean valid, String userId, String username, String role) {}
