package com.sportspulse.auth.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.auth.dto.requests.RegisterRequest;
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

@DisplayName("RegisterRequest")
class RegisterRequestTest {

  private static Validator validator;

  // -------------------------------------------------------------------------
  // Fixtures
  // -------------------------------------------------------------------------
  private static final String VALID_USERNAME = "johndoe";
  private static final String VALID_EMAIL = "john@example.com";
  private static final String VALID_PASSWORD = "Secure@123";

  @BeforeAll
  static void setUpValidator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  private RegisterRequest buildValidRequest() {
    return RegisterRequest.builder()
        .username(VALID_USERNAME)
        .email(VALID_EMAIL)
        .password(VALID_PASSWORD)
        .build();
  }

  private Set<ConstraintViolation<RegisterRequest>> validate(RegisterRequest request) {
    return validator.validate(request);
  }

  private void assertViolationOnField(
      Set<ConstraintViolation<RegisterRequest>> violations, String field, String expectedMessage) {
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
      assertThat(validate(buildValidRequest())).isEmpty();
    }

    @ParameterizedTest(name = "username = [{0}]")
    @ValueSource(strings = {"a", "john_doe", "user123", "UPPERCASE", "mix3d_User"})
    @DisplayName("should accept any non-blank username")
    void shouldAcceptAnyNonBlankUsername(String username) {
      RegisterRequest request = buildValidRequest();
      request.setUsername(username);

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
      RegisterRequest request = buildValidRequest();
      request.setEmail(email);

      assertThat(validate(request)).isEmpty();
    }

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(strings = {"Secure@123", "MyP@ssw0rd", "C0mpl3x!Pass", "Abcdef1!"})
    @DisplayName("should accept passwords that satisfy all password rules")
    void shouldAcceptStrongPasswords(String password) {
      RegisterRequest request = buildValidRequest();
      request.setPassword(password);

      assertThat(validate(request)).isEmpty();
    }
  }

  // =========================================================================
  // Username
  // =========================================================================
  @Nested
  @DisplayName("username field")
  class UsernameField {

    @ParameterizedTest(name = "username = [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("should violate @NotBlank when username is null, empty or blank")
    void shouldViolateNotBlankForNullEmptyOrBlank(String username) {
      RegisterRequest request = buildValidRequest();
      request.setUsername(username);

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertViolationOnField(violations, "username", "Username is required");
    }

    @ParameterizedTest(name = "username = [{0}]")
    @ValueSource(
        strings = {
          "john doe", // middle space
          " johndoe", // leading space
          "johndoe ", // trailing space
          "john\tdoe", // tab in the middle
          "john  doe", // multiple spaces
        })
    @DisplayName("should violate @Pattern when username contains whitespace")
    void shouldViolatePatternWhenUsernameContainsSpaces(String username) {
      RegisterRequest request = buildValidRequest();
      request.setUsername(username);

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertViolationOnField(violations, "username", "Username must not contain spaces");
    }

    @ParameterizedTest(name = "username = [{0}]")
    @ValueSource(strings = {"a", "john_doe", "user123", "UPPERCASE", "mix3d_User", "user@name"})
    @DisplayName("should accept any non-blank username without spaces")
    void shouldAcceptAnyNonBlankUsernameWithoutSpaces(String username) {
      RegisterRequest request = buildValidRequest();
      request.setUsername(username);

      assertThat(validate(request)).isEmpty();
    }

    @Test
    @DisplayName(
        "should produce two violations when username is empty (violates @NotBlank and @Pattern)")
    void shouldProduceTwoViolationsForEmptyUsername() {
      RegisterRequest request = buildValidRequest();
      request.setUsername("");

      Set<ConstraintViolation<RegisterRequest>> violations =
          validate(request).stream()
              .filter(v -> v.getPropertyPath().toString().equals("username"))
              .collect(java.util.stream.Collectors.toSet());

      assertThat(violations).hasSize(2);
      assertThat(violations).anyMatch(v -> v.getMessage().equals("Username is required"));
      assertThat(violations)
          .anyMatch(v -> v.getMessage().equals("Username must not contain spaces"));
    }

    @Test
    @DisplayName("should produce exactly one violation when username contains spaces")
    void shouldProduceExactlyOneViolationForUsernameWithSpaces() {
      RegisterRequest request = buildValidRequest();
      request.setUsername("john doe");

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertThat(violations)
          .filteredOn(v -> v.getPropertyPath().toString().equals("username"))
          .hasSize(1);
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
      RegisterRequest request = buildValidRequest();
      request.setEmail(email);

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertViolationOnField(violations, "email", "Email is required");
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
      RegisterRequest request = buildValidRequest();
      request.setEmail(email);

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertViolationOnField(violations, "email", "Invalid email format");
    }

    @Test
    @DisplayName("should produce exactly one violation when only email is invalid")
    void shouldProduceExactlyOneViolationForEmail() {
      RegisterRequest request = buildValidRequest();
      request.setEmail("invalid-email");

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertThat(violations)
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
      RegisterRequest request = buildValidRequest();
      request.setPassword(password);

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertViolationOnField(violations, "password", "Password is required");
    }

    @ParameterizedTest(name = "password = [{0}]")
    @ValueSource(
        strings = {
          "nouppercase1!", // no uppercase
          "NOLOWERCASE1!", // no lowercase
          "NoDigits!!", // no digit
          "NoSpecial1", // no special char
          "Sh@rt1", // too short
          "Has White Space1!" // whitespace
        })
    @DisplayName("should violate @ValidPassword for weak passwords")
    void shouldViolateValidPasswordForWeakPasswords(String password) {
      RegisterRequest request = buildValidRequest();
      request.setPassword(password);

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertThat(violations)
          .filteredOn(v -> v.getPropertyPath().toString().equals("password"))
          .isNotEmpty();
    }

    @Test
    @DisplayName("should produce exactly one violation when only password is weak")
    void shouldProduceExactlyOneViolationForPassword() {
      RegisterRequest request = buildValidRequest();
      request.setPassword("weakpassword");

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertThat(violations)
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
      RegisterRequest request =
          RegisterRequest.builder().username("").email("not-an-email").password("weak").build();

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertThat(violations).hasSizeGreaterThanOrEqualTo(3);

      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
      assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("should report violations for all null fields")
    void shouldReportAllViolationsForNullFields() {
      RegisterRequest request = RegisterRequest.builder().build();

      Set<ConstraintViolation<RegisterRequest>> violations = validate(request);

      assertThat(violations).hasSizeGreaterThanOrEqualTo(3);

      Set<String> violatedFields = new java.util.HashSet<>();
      violations.forEach(v -> violatedFields.add(v.getPropertyPath().toString()));

      assertThat(violatedFields).containsExactlyInAnyOrder("username", "email", "password");
    }
  }
}
