package com.sportspulse.fixtures.dtos.requests;

import com.sportspulse.fixtures.utils.Status;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FixturesQueryParamsRequest validation")
class FixturesQueryParamsRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Set<ConstraintViolation<FixturesQueryParamsRequest>> validate(
            FixturesQueryParamsRequest request) {
        return validator.validate(request);
    }

    private FixturesQueryParamsRequest validRequest() {
        return FixturesQueryParamsRequest.builder()
                .league(39)
                .team(33)
                .date(LocalDate.of(2026, 6, 27))
                .status(Status.NS)
                .build();
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("valid requests")
    class ValidRequests {

        @Test
        @DisplayName("passes validation when all fields are valid")
        void passesWhenAllFieldsAreValid() {
            assertThat(validate(validRequest())).isEmpty();
        }

        @Test
        @DisplayName("passes validation when league is null (optional field)")
        void passesWhenLeagueIsNull() {
            FixturesQueryParamsRequest request = validRequest();
            request.setLeague(null);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        @DisplayName("passes validation when team is null (optional field)")
        void passesWhenTeamIsNull() {
            FixturesQueryParamsRequest request = validRequest();
            request.setTeam(null);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        @DisplayName("passes validation when status is null (optional field)")
        void passesWhenStatusIsNull() {
            FixturesQueryParamsRequest request = validRequest();
            request.setStatus(null);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        @DisplayName("passes validation when only date is provided and the rest are null")
        void passesWhenOnlyDateIsProvided() {
            FixturesQueryParamsRequest request = FixturesQueryParamsRequest.builder()
                    .date(LocalDate.now())
                    .build();

            assertThat(validate(request)).isEmpty();
        }

        @Test
        @DisplayName("passes validation when league is exactly 1 (boundary: minimum positive value)")
        void passesWhenLeagueIsOne() {
            FixturesQueryParamsRequest request = validRequest();
            request.setLeague(1);

            assertThat(validate(request)).isEmpty();
        }

        @Test
        @DisplayName("passes validation when team is exactly 1 (boundary: minimum positive value)")
        void passesWhenTeamIsOne() {
            FixturesQueryParamsRequest request = validRequest();
            request.setTeam(1);

            assertThat(validate(request)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // league violations
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("league field validation")
    class LeagueValidation {

        @Test
        @DisplayName("fails when league is 0 (not positive)")
        void failsWhenLeagueIsZero() {
            FixturesQueryParamsRequest request = validRequest();
            request.setLeague(0);

            Set<ConstraintViolation<FixturesQueryParamsRequest>> violations = validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("league must be greater than 0");
        }

        @Test
        @DisplayName("fails when league is negative")
        void failsWhenLeagueIsNegative() {
            FixturesQueryParamsRequest request = validRequest();
            request.setLeague(-1);

            Set<ConstraintViolation<FixturesQueryParamsRequest>> violations = validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("league must be greater than 0");
        }

        @Test
        @DisplayName("violation is on the 'league' property path")
        void violationPropertyPathIsLeague() {
            FixturesQueryParamsRequest request = validRequest();
            request.setLeague(-99);

            Set<ConstraintViolation<FixturesQueryParamsRequest>> violations = validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("league");
        }
    }

    // -------------------------------------------------------------------------
    // team violations
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("team field validation")
    class TeamValidation {

        @Test
        @DisplayName("fails when team is 0 (not positive)")
        void failsWhenTeamIsZero() {
            FixturesQueryParamsRequest request = validRequest();
            request.setTeam(0);

            Set<ConstraintViolation<FixturesQueryParamsRequest>> violations = validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("team must be greater than 0");
        }

        @Test
        @DisplayName("fails when team is negative")
        void failsWhenTeamIsNegative() {
            FixturesQueryParamsRequest request = validRequest();
            request.setTeam(-1);

            Set<ConstraintViolation<FixturesQueryParamsRequest>> violations = validate(request);

            assertThat(violations).hasSize(1);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("team must be greater than 0");
        }

        @Test
        @DisplayName("violation is on the 'team' property path")
        void violationPropertyPathIsTeam() {
            FixturesQueryParamsRequest request = validRequest();
            request.setTeam(-99);

            Set<ConstraintViolation<FixturesQueryParamsRequest>> violations = validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("team");
        }
    }

    // -------------------------------------------------------------------------
    // Multiple simultaneous violations
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("multiple violations")
    class MultipleViolations {

        @Test
        @DisplayName("reports both violations when league and team are both invalid")
        void reportsBothViolationsWhenLeagueAndTeamAreInvalid() {
            FixturesQueryParamsRequest request = validRequest();
            request.setLeague(0);
            request.setTeam(-5);

            Set<ConstraintViolation<FixturesQueryParamsRequest>> violations = validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "league must be greater than 0",
                            "team must be greater than 0");
        }

        @Test
        @DisplayName("violation property paths are 'league' and 'team' when both are invalid")
        void violationPathsAreLeagueAndTeamWhenBothInvalid() {
            FixturesQueryParamsRequest request = validRequest();
            request.setLeague(-1);
            request.setTeam(-1);

            Set<ConstraintViolation<FixturesQueryParamsRequest>> violations = validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("league", "team");
        }
    }

    // -------------------------------------------------------------------------
    // Default value of date
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("date field default value")
    class DateDefault {

        @Test
        @DisplayName("date defaults to today when built without explicit date")
        void dateDefaultsToTodayWhenNotSet() {
            FixturesQueryParamsRequest request = FixturesQueryParamsRequest.builder().build();

            assertThat(request.getDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("date defaults to today when created via no-args constructor")
        void dateDefaultsToTodayViaNoArgsConstructor() {
            FixturesQueryParamsRequest request = new FixturesQueryParamsRequest();

            assertThat(request.getDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("explicit date overrides the default")
        void explicitDateOverridesDefault() {
            LocalDate customDate = LocalDate.of(2025, 1, 15);
            FixturesQueryParamsRequest request = FixturesQueryParamsRequest.builder()
                    .date(customDate)
                    .build();

            assertThat(request.getDate()).isEqualTo(customDate);
        }
    }
}