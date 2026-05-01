package com.sportspulse.standings.dto.response;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.sportspulse.standings.dtos.responses.ErrorResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

  // ─── Builder ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Builder should set all explicit fields correctly")
  void builder_shouldPopulateAllFields() {
    Instant timestamp = Instant.parse("2024-01-15T10:30:00.000Z");

    ErrorResponse response =
        ErrorResponse.builder()
            .code(404)
            .name("Not Found")
            .description("The requested resource does not exist")
            .timestamp(timestamp)
            .build();

    assertThat(response.getCode()).isEqualTo(404);
    assertThat(response.getName()).isEqualTo("Not Found");
    assertThat(response.getDescription()).isEqualTo("The requested resource does not exist");
    assertThat(response.getTimestamp()).isEqualTo(timestamp);
  }

  @Test
  @DisplayName("Builder should default timestamp to current time truncated to millis when not set")
  void builder_whenTimestampNotSet_shouldDefaultToCurrentTimeTruncatedToMillis() {
    Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(500)
            .name("Internal Server Error")
            .description("Unexpected error occurred")
            .build();

    Instant after = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    assertThat(response.getTimestamp())
        .isNotNull()
        .isAfterOrEqualTo(before)
        .isBeforeOrEqualTo(after);
  }

  @Test
  @DisplayName("Default timestamp should have no sub-millisecond precision")
  void builder_whenTimestampNotSet_shouldHaveMillisPrecisionOnly() {
    ErrorResponse response =
        ErrorResponse.builder().code(400).name("Bad Request").description("Invalid input").build();

    long nanos = response.getTimestamp().getNano();
    assertThat(nanos % 1_000_000).isZero();
  }

  @Test
  @DisplayName("Builder should allow overriding the default timestamp")
  void builder_whenTimestampExplicitlySet_shouldOverrideDefault() {
    Instant customTimestamp = Instant.parse("2020-06-01T00:00:00.000Z");

    ErrorResponse response =
        ErrorResponse.builder()
            .code(401)
            .name("Unauthorized")
            .description("Authentication required")
            .timestamp(customTimestamp)
            .build();

    assertThat(response.getTimestamp()).isEqualTo(customTimestamp);
  }

  // ─── Immutability (@Value) ────────────────────────────────────────────────

  @Test
  @DisplayName("ErrorResponse should not expose any setter methods")
  void value_shouldExposeNoSetters() {
    Method[] methods = ErrorResponse.class.getMethods();

    assertThat(methods)
        .extracting(Method::getName)
        .doesNotContain("setCode", "setName", "setDescription", "setTimestamp");
  }

  @Test
  @DisplayName("ErrorResponse fields should be declared final")
  void value_allFieldsShouldBeFinal() {
    Field[] fields = ErrorResponse.class.getDeclaredFields();

    assertThat(fields)
        .allSatisfy(
            field ->
                assertThat(Modifier.isFinal(field.getModifiers()))
                    .as("Field '%s' should be final", field.getName())
                    .isTrue());
  }

  // ─── equals & hashCode ────────────────────────────────────────────────────

  @Test
  @DisplayName("equals() should return true for two instances with identical fields")
  void equals_whenSameFields_shouldReturnTrue() {
    Instant timestamp = Instant.parse("2024-03-10T12:00:00.000Z");

    ErrorResponse a =
        ErrorResponse.builder()
            .code(200)
            .name("OK")
            .description("Success")
            .timestamp(timestamp)
            .build();

    ErrorResponse b =
        ErrorResponse.builder()
            .code(200)
            .name("OK")
            .description("Success")
            .timestamp(timestamp)
            .build();

    assertThat(a).isEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when code differs")
  void equals_whenCodeDiffers_shouldReturnFalse() {
    Instant timestamp = Instant.parse("2024-03-10T12:00:00.000Z");

    ErrorResponse a =
        ErrorResponse.builder()
            .code(400)
            .name("Bad Request")
            .description("desc")
            .timestamp(timestamp)
            .build();
    ErrorResponse b =
        ErrorResponse.builder()
            .code(404)
            .name("Bad Request")
            .description("desc")
            .timestamp(timestamp)
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when name differs")
  void equals_whenNameDiffers_shouldReturnFalse() {
    Instant timestamp = Instant.parse("2024-03-10T12:00:00.000Z");

    ErrorResponse a =
        ErrorResponse.builder()
            .code(400)
            .name("Bad Request")
            .description("desc")
            .timestamp(timestamp)
            .build();
    ErrorResponse b =
        ErrorResponse.builder()
            .code(400)
            .name("Conflict")
            .description("desc")
            .timestamp(timestamp)
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when description differs")
  void equals_whenDescriptionDiffers_shouldReturnFalse() {
    Instant timestamp = Instant.parse("2024-03-10T12:00:00.000Z");

    ErrorResponse a =
        ErrorResponse.builder()
            .code(400)
            .name("Bad Request")
            .description("desc A")
            .timestamp(timestamp)
            .build();
    ErrorResponse b =
        ErrorResponse.builder()
            .code(400)
            .name("Bad Request")
            .description("desc B")
            .timestamp(timestamp)
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when timestamp differs")
  void equals_whenTimestampDiffers_shouldReturnFalse() {
    ErrorResponse a =
        ErrorResponse.builder()
            .code(400)
            .name("Bad Request")
            .description("desc")
            .timestamp(Instant.parse("2024-01-01T00:00:00.000Z"))
            .build();
    ErrorResponse b =
        ErrorResponse.builder()
            .code(400)
            .name("Bad Request")
            .description("desc")
            .timestamp(Instant.parse("2024-06-01T00:00:00.000Z"))
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("hashCode() should be equal for two instances with identical fields")
  void hashCode_whenSameFields_shouldBeEqual() {
    Instant timestamp = Instant.parse("2024-03-10T12:00:00.000Z");

    ErrorResponse a =
        ErrorResponse.builder()
            .code(200)
            .name("OK")
            .description("Success")
            .timestamp(timestamp)
            .build();
    ErrorResponse b =
        ErrorResponse.builder()
            .code(200)
            .name("OK")
            .description("Success")
            .timestamp(timestamp)
            .build();

    assertThat(a).hasSameHashCodeAs(b);
  }

  @Test
  @DisplayName("hashCode() should differ when any field differs")
  void hashCode_whenFieldsDiffer_shouldDiffer() {
    Instant timestamp = Instant.parse("2024-03-10T12:00:00.000Z");

    ErrorResponse a =
        ErrorResponse.builder()
            .code(200)
            .name("OK")
            .description("Success")
            .timestamp(timestamp)
            .build();
    ErrorResponse b =
        ErrorResponse.builder()
            .code(500)
            .name("Error")
            .description("Failure")
            .timestamp(timestamp)
            .build();

    assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
  }

  // ─── toString ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("toString() should contain all field values")
  void toString_shouldContainAllFieldValues() {
    Instant timestamp = Instant.parse("2024-03-10T12:00:00.000Z");

    ErrorResponse response =
        ErrorResponse.builder()
            .code(403)
            .name("Forbidden")
            .description("Access denied")
            .timestamp(timestamp)
            .build();

    assertThat(response.toString())
        .contains("403")
        .contains("Forbidden")
        .contains("Access denied")
        .contains(timestamp.toString());
  }

  // ─── Getters ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Getters should return the values set via builder")
  void getters_shouldReturnCorrectValues() {
    Instant timestamp = Instant.parse("2024-07-20T08:15:30.123Z");

    ErrorResponse response =
        ErrorResponse.builder()
            .code(503)
            .name("Service Unavailable")
            .description("Downstream service is down")
            .timestamp(timestamp)
            .build();

    assertThat(response.getCode()).isEqualTo(503);
    assertThat(response.getName()).isEqualTo("Service Unavailable");
    assertThat(response.getDescription()).isEqualTo("Downstream service is down");
    assertThat(response.getTimestamp()).isEqualTo(timestamp);
  }
}
