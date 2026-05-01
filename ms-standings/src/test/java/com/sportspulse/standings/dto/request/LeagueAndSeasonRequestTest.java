package com.sportspulse.standings.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.standings.dtos.request.LeagueAndSeasonRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LeagueAndSeasonRequestTest {

  private static Validator validator;

  @BeforeAll
  static void setUpValidator() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  private Set<ConstraintViolation<LeagueAndSeasonRequest>> validate(
      LeagueAndSeasonRequest request) {
    return validator.validate(request);
  }

  private LeagueAndSeasonRequest validRequest() {
    return LeagueAndSeasonRequest.builder().league(39).season("2023").build();
  }

  // ─── Valid request ────────────────────────────────────────────────────────

  @Test
  @DisplayName("Valid request should produce no constraint violations")
  void validRequest_shouldHaveNoViolations() {
    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(validRequest());

    assertThat(violations).isEmpty();
  }

  // ─── league ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("league = 0 should violate @Min(1)")
  void league_whenZero_shouldFailMinConstraint() {
    LeagueAndSeasonRequest request = validRequest();
    request.setLeague(0);

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(1)
        .allSatisfy(
            v -> {
              assertThat(v.getPropertyPath().toString()).isEqualTo("league");
              assertThat(v.getMessage()).isEqualTo("League must be greater than 0");
            });
  }

  @Test
  @DisplayName("league = negative value should violate @Min(1)")
  void league_whenNegative_shouldFailMinConstraint() {
    LeagueAndSeasonRequest request = validRequest();
    request.setLeague(-5);

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(1)
        .allSatisfy(
            v -> {
              assertThat(v.getPropertyPath().toString()).isEqualTo("league");
              assertThat(v.getMessage()).isEqualTo("League must be greater than 0");
            });
  }

  @Test
  @DisplayName("league = 1 (boundary) should be valid")
  void league_whenOne_shouldBeValid() {
    LeagueAndSeasonRequest request = validRequest();
    request.setLeague(1);

    assertThat(validate(request)).isEmpty();
  }

  // ─── season ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("season = null should violate @NotBlank")
  void season_whenNull_shouldFailNotBlankConstraint() {
    LeagueAndSeasonRequest request = validRequest();
    request.setSeason(null);

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(1)
        .allSatisfy(
            v -> {
              assertThat(v.getPropertyPath().toString()).isEqualTo("season");
              assertThat(v.getMessage()).isEqualTo("Season cannot be blank");
            });
  }

  @Test
  @DisplayName("season = blank string should violate @NotBlank and @Pattern")
  void season_whenBlank_shouldFailBothNotBlankAndPatternConstraints() {
    LeagueAndSeasonRequest request = validRequest();
    request.setSeason("   ");

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(2)
        .allSatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("season"));
  }

  @Test
  @DisplayName("season = empty string should violate @NotBlank and @Pattern")
  void season_whenEmpty_shouldFailBothNotBlankAndPatternConstraints() {
    LeagueAndSeasonRequest request = validRequest();
    request.setSeason("");

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(2)
        .allSatisfy(v -> assertThat(v.getPropertyPath().toString()).isEqualTo("season"));
  }

  @Test
  @DisplayName("season with 3 digits should violate @Pattern")
  void season_whenThreeDigits_shouldFailPatternConstraint() {
    LeagueAndSeasonRequest request = validRequest();
    request.setSeason("202");

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(1)
        .allSatisfy(
            v -> {
              assertThat(v.getPropertyPath().toString()).isEqualTo("season");
              assertThat(v.getMessage()).isEqualTo("Season must be numeric and 4 digits");
            });
  }

  @Test
  @DisplayName("season with 5 digits should violate @Pattern")
  void season_whenFiveDigits_shouldFailPatternConstraint() {
    LeagueAndSeasonRequest request = validRequest();
    request.setSeason("20233");

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(1)
        .allSatisfy(
            v -> {
              assertThat(v.getPropertyPath().toString()).isEqualTo("season");
              assertThat(v.getMessage()).isEqualTo("Season must be numeric and 4 digits");
            });
  }

  @Test
  @DisplayName("season with non-numeric characters should violate @Pattern")
  void season_whenAlphanumeric_shouldFailPatternConstraint() {
    LeagueAndSeasonRequest request = validRequest();
    request.setSeason("20AB");

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(1)
        .allSatisfy(
            v -> {
              assertThat(v.getPropertyPath().toString()).isEqualTo("season");
              assertThat(v.getMessage()).isEqualTo("Season must be numeric and 4 digits");
            });
  }

  @Test
  @DisplayName("season with only letters should violate @Pattern")
  void season_whenAlphabetic_shouldFailPatternConstraint() {
    LeagueAndSeasonRequest request = validRequest();
    request.setSeason("abcd");

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations)
        .hasSize(1)
        .allSatisfy(
            v -> {
              assertThat(v.getPropertyPath().toString()).isEqualTo("season");
              assertThat(v.getMessage()).isEqualTo("Season must be numeric and 4 digits");
            });
  }

  // ─── Multiple violations ──────────────────────────────────────────────────

  @Test
  @DisplayName("league = 0 and season = null should produce two violations")
  void request_whenLeagueZeroAndSeasonNull_shouldFailBothConstraints() {
    LeagueAndSeasonRequest request =
        LeagueAndSeasonRequest.builder().league(0).season(null).build();

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations).hasSize(2);

    Set<String> fields =
        violations.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());

    assertThat(fields).containsExactlyInAnyOrder("league", "season");
  }

  @Test
  @DisplayName("league = 0 and season = invalid pattern should produce two violations")
  void request_whenLeagueZeroAndSeasonInvalid_shouldFailBothConstraints() {
    LeagueAndSeasonRequest request =
        LeagueAndSeasonRequest.builder().league(0).season("XYZ").build();

    Set<ConstraintViolation<LeagueAndSeasonRequest>> violations = validate(request);

    assertThat(violations).hasSize(2);
  }

  // ─── Builder & constructors ───────────────────────────────────────────────

  @Test
  @DisplayName("Builder should set all fields correctly")
  void builder_shouldPopulateAllFields() {
    LeagueAndSeasonRequest request =
        LeagueAndSeasonRequest.builder().league(39).season("2023").build();

    assertThat(request.getLeague()).isEqualTo(39);
    assertThat(request.getSeason()).isEqualTo("2023");
  }

  @Test
  @DisplayName("No-args constructor should initialise fields to defaults")
  void noArgsConstructor_shouldInitialiseDefaults() {
    LeagueAndSeasonRequest request = new LeagueAndSeasonRequest();

    assertThat(request.getLeague()).isZero();
    assertThat(request.getSeason()).isNull();
  }

  @Test
  @DisplayName("All-args constructor should set all fields correctly")
  void allArgsConstructor_shouldSetAllFields() {
    LeagueAndSeasonRequest request = new LeagueAndSeasonRequest(39, "2023");

    assertThat(request.getLeague()).isEqualTo(39);
    assertThat(request.getSeason()).isEqualTo("2023");
  }

  // ─── Lombok @Data ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("Setters generated by @Data should update fields")
  void setters_shouldUpdateFields() {
    LeagueAndSeasonRequest request = new LeagueAndSeasonRequest();
    request.setLeague(10);
    request.setSeason("2024");

    assertThat(request.getLeague()).isEqualTo(10);
    assertThat(request.getSeason()).isEqualTo("2024");
  }

  @Test
  @DisplayName("equals() should return true for two requests with identical fields")
  void equals_whenSameFields_shouldReturnTrue() {
    LeagueAndSeasonRequest a = new LeagueAndSeasonRequest(39, "2023");
    LeagueAndSeasonRequest b = new LeagueAndSeasonRequest(39, "2023");

    assertThat(a).isEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when fields differ")
  void equals_whenDifferentFields_shouldReturnFalse() {
    LeagueAndSeasonRequest a = new LeagueAndSeasonRequest(39, "2023");
    LeagueAndSeasonRequest b = new LeagueAndSeasonRequest(40, "2023");

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("hashCode() should be equal for two requests with identical fields")
  void hashCode_whenSameFields_shouldBeEqual() {
    LeagueAndSeasonRequest a = new LeagueAndSeasonRequest(39, "2023");
    LeagueAndSeasonRequest b = new LeagueAndSeasonRequest(39, "2023");

    assertThat(a).hasSameHashCodeAs(b);
  }

  @Test
  @DisplayName("toString() should contain field values")
  void toString_shouldContainFieldValues() {
    LeagueAndSeasonRequest request = new LeagueAndSeasonRequest(39, "2023");

    assertThat(request.toString()).contains("39").contains("2023");
  }
}
