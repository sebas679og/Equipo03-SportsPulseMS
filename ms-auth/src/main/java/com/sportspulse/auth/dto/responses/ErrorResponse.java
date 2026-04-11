package com.sportspulse.auth.dto.responses;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/** Response DTO for error information. */
@Value
@Builder
public class ErrorResponse {

  int code;

  String name;

  String description;

  @Builder.Default Instant timestamp = Instant.ofEpochMilli(Instant.now().toEpochMilli());
}
