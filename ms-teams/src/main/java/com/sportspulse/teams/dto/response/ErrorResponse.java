package com.sportspulse.teams.dto.response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record ErrorResponse(
        String error,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(
                error,
                message,
                Instant.now().truncatedTo(ChronoUnit.MILLIS)
        );
    }
}
