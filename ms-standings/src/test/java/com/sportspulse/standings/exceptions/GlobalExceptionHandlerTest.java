package com.sportspulse.standings.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sportspulse.standings.dtos.responses.ErrorResponse;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private void assertErrorResponse(
      ResponseEntity<ErrorResponse> response,
      HttpStatus expectedStatus,
      String expectedDescription) {

    assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCode()).isEqualTo(expectedStatus.value());
    assertThat(response.getBody().getName()).isEqualTo(expectedStatus.getReasonPhrase());
    assertThat(response.getBody().getDescription()).isEqualTo(expectedDescription);
  }

  // ─── CustomUnauthorizedException ──────────────────────────────────────────

  @Test
  @DisplayName("handleCustomUnauthorizedException() should return 401 with exception message")
  void handleCustomUnauthorizedException_shouldReturn401() {
    CustomUnauthorizedException ex = new CustomUnauthorizedException("Invalid token");

    ResponseEntity<ErrorResponse> response = handler.handleCustomUnauthorizedException(ex);

    assertErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token");
  }

  // ─── CustomTooManyRequestsException ───────────────────────────────────────

  @Test
  @DisplayName("handleCustomTooManyRequestsException() should return 429 with exception message")
  void handleCustomTooManyRequestsException_shouldReturn429() {
    CustomTooManyRequestsException ex = new CustomTooManyRequestsException("Rate limit exceeded");

    ResponseEntity<ErrorResponse> response = handler.handleCustomTooManyRequestsException(ex);

    assertErrorResponse(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
  }

  // ─── CustomServiceUnavailableException ────────────────────────────────────

  @Test
  @DisplayName(
      "handlerCustomServiceUnavailableException() should return 503 with exception message")
  void handlerCustomServiceUnavailableException_shouldReturn503() {
    CustomServiceUnavailableException ex = new CustomServiceUnavailableException("Service is down");

    ResponseEntity<ErrorResponse> response = handler.handlerCustomServiceUnavailableException(ex);

    assertErrorResponse(response, HttpStatus.SERVICE_UNAVAILABLE, "Service is down");
  }

  // ─── CustomNotFoundException ──────────────────────────────────────────────

  @Test
  @DisplayName("handlerCustomNotFoundException() should return 404 with exception message")
  void handlerCustomNotFoundException_shouldReturn404() {
    CustomNotFoundException ex = new CustomNotFoundException("Resource not found");

    ResponseEntity<ErrorResponse> response = handler.handlerCustomNotFoundException(ex);

    assertErrorResponse(response, HttpStatus.NOT_FOUND, "Resource not found");
  }

  // ─── CustomBadGatewayException ────────────────────────────────────────────

  @Test
  @DisplayName("handlerCustomBadGatewayException() should return 502 with exception message")
  void handlerCustomBadGatewayException_shouldReturn502() {
    CustomBadGatewayException ex = new CustomBadGatewayException("Upstream error");

    ResponseEntity<ErrorResponse> response = handler.handlerCustomBadGatewayException(ex);

    assertErrorResponse(response, HttpStatus.BAD_GATEWAY, "Upstream error");
  }

  // ─── CustomBadRequestException ────────────────────────────────────────────

  @Test
  @DisplayName("handlerCustomBadRequestException() should return 400 with exception message")
  void handlerCustomBadRequestException_shouldReturn400() {
    CustomBadRequestException ex = new CustomBadRequestException("Malformed request");

    ResponseEntity<ErrorResponse> response = handler.handlerCustomBadRequestException(ex);

    assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Malformed request");
  }

  // ─── MethodArgumentTypeMismatchException ──────────────────────────────────

  @Test
  @DisplayName(
      "handlerMethodArgumentTypeMismatchException() should return 400 with formatted message")
  void handlerMethodArgumentTypeMismatchException_shouldReturn400WithFormattedMessage() {
    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getName()).thenReturn("league");
    when(ex.getValue()).thenReturn("abc");

    ResponseEntity<ErrorResponse> response = handler.handlerMethodArgumentTypeMismatchException(ex);

    assertErrorResponse(
        response,
        HttpStatus.BAD_REQUEST,
        "The parameter 'league' received an invalid value: 'abc'");
  }

  @Test
  @DisplayName("handlerMethodArgumentTypeMismatchException() should include null value in message")
  void handlerMethodArgumentTypeMismatchException_whenValueIsNull_shouldIncludeNullInMessage() {
    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getName()).thenReturn("season");
    when(ex.getValue()).thenReturn(null);

    ResponseEntity<ErrorResponse> response = handler.handlerMethodArgumentTypeMismatchException(ex);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription()).contains("season").contains("null");
  }

  // ─── MethodArgumentNotValidException – field errors ───────────────────────

  @Test
  @DisplayName("handleValidationException() should return 400 with field error default message")
  void handleValidationException_whenFieldErrorWithDefaultMessage_shouldReturnDefaultMessage() {
    FieldError fieldError = new FieldError("request", "league", "League must be greater than 0");

    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(fieldError);

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(
            new MethodParameter(
                Objects.requireNonNull(
                    ReflectionUtils.findMethod(GlobalExceptionHandlerTest.class, "dummyMethod")),
                -1),
            bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

    assertErrorResponse(response, HttpStatus.BAD_REQUEST, "League must be greater than 0");
  }

  @Test
  @DisplayName(
      "handleValidationException() should return 'Invalid value for field' "
          + "when message starts with 'Failed to convert'")
  void handleValidationException_whenMessageStartsWithFailedToConvert_shouldReturnGenericMessage() {
    FieldError fieldError =
        new FieldError(
            "request", "league", null, false, null, null, "Failed to convert value of type");

    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(fieldError);

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(
            new MethodParameter(
                Objects.requireNonNull(
                    ReflectionUtils.findMethod(GlobalExceptionHandlerTest.class, "dummyMethod")),
                -1),
            bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription()).isEqualTo("Invalid value for field 'league'");
  }

  @Test
  @DisplayName(
      "handleValidationException() should return "
          + "'Invalid value for field' when default message is null")
  void handleValidationException_whenDefaultMessageIsNull_shouldReturnGenericMessage() {
    FieldError fieldError = new FieldError("request", "season", null, false, null, null, null);

    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(fieldError);

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(
            new MethodParameter(
                Objects.requireNonNull(
                    ReflectionUtils.findMethod(GlobalExceptionHandlerTest.class, "dummyMethod")),
                -1),
            bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription()).isEqualTo("Invalid value for field 'season'");
  }

  @Test
  @DisplayName("handleValidationException() should concatenate multiple field errors with ', '")
  void handleValidationException_whenMultipleFieldErrors_shouldConcatenateMessages() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(new FieldError("request", "league", "League must be greater than 0"));
    bindingResult.addError(new FieldError("request", "season", "Season cannot be blank"));

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(
            new MethodParameter(
                Objects.requireNonNull(
                    ReflectionUtils.findMethod(GlobalExceptionHandlerTest.class, "dummyMethod")),
                -1),
            bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription())
        .contains("League must be greater than 0")
        .contains("Season cannot be blank")
        .contains(", ");
  }

  @Test
  @DisplayName("handleValidationException() should handle a global (non-field) ObjectError")
  void handleValidationException_whenGlobalObjectError_shouldUseDefaultMessage() {
    BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(new ObjectError("request", "Object-level constraint violated"));

    MethodArgumentNotValidException ex =
        new MethodArgumentNotValidException(
            new MethodParameter(
                Objects.requireNonNull(
                    ReflectionUtils.findMethod(GlobalExceptionHandlerTest.class, "dummyMethod")),
                -1),
            bindingResult);

    ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getDescription()).isEqualTo("Object-level constraint violated");
  }

  // ─── ErrorResponse body structure ─────────────────────────────────────────

  @Test
  @DisplayName("buildErrorResponse() should populate code, name and description correctly")
  void buildErrorResponse_shouldPopulateAllBodyFields() {
    CustomNotFoundException ex = new CustomNotFoundException("Not here");

    ResponseEntity<ErrorResponse> response = handler.handlerCustomNotFoundException(ex);

    ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getCode()).isEqualTo(404);
    assertThat(body.getName()).isEqualTo("Not Found");
    assertThat(body.getDescription()).isEqualTo("Not here");
    assertThat(body.getTimestamp()).isNotNull();
  }

  // ─── Dummy method required for MethodParameter reflection ─────────────────

  @SuppressWarnings("unused")
  public void dummyMethod() {}
}
