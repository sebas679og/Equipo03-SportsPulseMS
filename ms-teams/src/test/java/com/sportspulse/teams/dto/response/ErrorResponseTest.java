package com.sportspulse.teams.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.teams.constants.Errors;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

  @Test
  void of_shouldSetAllFieldsCorrectly() {

    String error = Errors.Code.MISSING_TOKEN;
    String message = Errors.Message.MISSING_TOKEN;

    ErrorResponse response = ErrorResponse.of(error, message);

    assertThat(response.error()).isEqualTo(error);
    assertThat(response.message()).isEqualTo(message);
    assertThat(response.timestamp()).isNotNull();
    assertThat(response.timestamp()).isBeforeOrEqualTo(Instant.now());
  }

  @Test
  void of_shouldTruncateTimestampToMillis() {
    ErrorResponse response = ErrorResponse.of("ERROR", "message");

    assertThat(response.timestamp().getNano() % 1_000_000).isZero();
  }

  @Test
  void of_twoCallsShouldHaveDifferentTimestamps() throws InterruptedException {

    ErrorResponse first = ErrorResponse.of("ERROR", "message");
    Thread.sleep(10);
    ErrorResponse second = ErrorResponse.of("ERROR", "message");

    assertThat(first.timestamp()).isBefore(second.timestamp());
  }
}
