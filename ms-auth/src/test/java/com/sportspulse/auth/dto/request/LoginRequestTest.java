package com.sportspulse.auth.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.auth.dto.requests.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("LoginRequest")
class LoginRequestTest {

  private static Validator validator;

  private static final String VALID_EMAIL = "john@example.com";
  private static final String VALID_PASSWORD = "Secure@123";

  @BeforeAll
  static void setUpValidator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  private Set<ConstraintViolation<LoginRequest>> validate(LoginRequest request) {
    return validator.validate(request);
  }

  private void assertViolationOnField(
      Set<ConstraintViolation<LoginRequest>> violations, String field, String expectedMessage) {
    assertThat(violations)
        .anyMatch(
            v ->
                v.getPropertyPath().toString().equals(field)
                    && v.getMessage().equals(expectedMessage));
  }

  // =========================================================================
  // Valid request
  // =========================================================================
  @Nested
  @DisplayName("when all fields are valid")
  class WhenAllFieldsAreValid {

    @Test
    @DisplayName("should produce no violations")
    void shouldProduceNoViolations() {
      LoginRequest request =
          LoginRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

      assertThat(validate(request)).isEmpty();
    }

    @ParameterizedTest(name = "email = [{0}]")
    @ValueSource(
        strings = {
          "user@domain.com",
          "user+tag@domain.co.uk",
          "firstname.lastname@company.org",
          "user123@sub.domain.io"
        })
    @DisplayName("should accept well-formed email addresses")
    void shouldAcceptWellFormedEmails(String email) {
      LoginRequest request = LoginRequest.builder().email(email).password(VALID_PASSWORD).build();

      assertThat(validate(request)).isEmpty();
    }

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {"anypassword", "12345678", "no-rules-here", "Secure@123"})
    @DisplayName("should accept any non-blank password")
    void shouldAcceptAnyNonBlankPassword(String password) {
      LoginRequest request = LoginRequest.builder().email(VALID_EMAIL).password(password).build();

      assertThat(validate(request)).isEmpty();
    }
  }

  // =========================================================================
  // Email
  // =========================================================================
  @Nested
  @DisplayName("email field")
  class EmailField {

    @ParameterizedTest(name = "email = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("should violate @NotBlank when email is null, empty or blank")
    void shouldViolateNotBlankForNullEmptyOrBlank(String email) {
      LoginRequest request = LoginRequest.builder().email(email).password(VALID_PASSWORD).build();

      assertViolationOnField(validate(request), "email", "Email is required");
    }

    @ParameterizedTest(name = "email = [{0}]")
    @ValueSource(
        strings = {
          "notanemail",
          "missing@",
          "@nodomain.com",
          "spaces in@email.com",
          "double@@domain.com",
          "plaintext"
        })
    @DisplayName("should violate @Email for malformed email addresses")
    void shouldViolateEmailForMalformedAddresses(String email) {
      LoginRequest request = LoginRequest.builder().email(email).password(VALID_PASSWORD).build();

      assertViolationOnField(validate(request), "email", "Invalid email format");
    }

    @Test
    @DisplayName("should produce exactly one violation when only email is invalid")
    void shouldProduceExactlyOneViolationForInvalidEmail() {
      LoginRequest request =
          LoginRequest.builder().email("invalid-email").password(VALID_PASSWORD).build();

      assertThat(validate(request))
          .filteredOn(v -> v.getPropertyPath().toString().equals("email"))
          .hasSize(1);
    }
  }

  // =========================================================================
  // Password
  // =========================================================================
  @Nested
  @DisplayName("password field")
  class PasswordField {

    @ParameterizedTest(name = "password = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    @DisplayName("should violate @NotBlank when password is null, empty or blank")
    void shouldViolateNotBlankForNullEmptyOrBlank(String password) {
      LoginRequest request = LoginRequest.builder().email(VALID_EMAIL).password(password).build();

      assertViolationOnField(validate(request), "password", "Password is required");
    }

    @Test
    @DisplayName("should produce exactly one violation when only password is blank")
    void shouldProduceExactlyOneViolationForBlankPassword() {
      LoginRequest request = LoginRequest.builder().email(VALID_EMAIL).password("").build();

      assertThat(validate(request))
          .filteredOn(v -> v.getPropertyPath().toString().equals("password"))
          .hasSize(1);
    }
  }

  // =========================================================================
  // Multiple violations
  // =========================================================================
  @Nested
  @DisplayName("when multiple fields are invalid")
  class WhenMultipleFieldsAreInvalid {

    @Test
    @DisplayName("should report violations for all invalid fields simultaneously")
    void shouldReportAllViolationsAtOnce() {
      LoginRequest request = LoginRequest.builder().email("not-an-email").password("").build();

      Set<ConstraintViolation<LoginRequest>> violations = validate(request);

      assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("should report violations for all null fields")
    void shouldReportAllViolationsForNullFields() {
      LoginRequest request = LoginRequest.builder().build();

      Set<ConstraintViolation<LoginRequest>> violations = validate(request);

      Set<String> violatedFields = new java.util.HashSet<>();
      violations.forEach(v -> violatedFields.add(v.getPropertyPath().toString()));

      assertThat(violatedFields).containsExactlyInAnyOrder("email", "password");
    }
  }
}
