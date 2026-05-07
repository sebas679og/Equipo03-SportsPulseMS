package com.sportspulse.leagues.dto.responses;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.Value;

/** Standard API error response for leagues service. */
@Value
@Builder
public class ErrorResponse {
  int code;
  String name;
  String description;

  @Builder.Default Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
}
