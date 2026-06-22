package com.sportspulse.fixtures.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.fixtures.dtos.responses.ErrorResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class JsonWriterTest {

  @Mock private ObjectMapper objectMapper;

  @Mock private HttpServletResponse response;

  @Mock private ServletOutputStream outputStream;

  @InjectMocks private JsonWriter jsonWriter;

  @BeforeEach
  void setUp() throws IOException {
    given(response.getOutputStream()).willReturn(outputStream);
  }

  // -------------------------------------------------------------------------
  // HTTP status and Content-Type headers
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("sendError() sets the HTTP status code on the response")
  void sendError_setsHttpStatusCode() throws IOException {
    jsonWriter.sendError(response, HttpStatus.UNAUTHORIZED, "Unauthorized access");

    then(response).should().setStatus(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("sendError() sets Content-Type to application/json")
  void sendError_setsContentTypeToApplicationJson() throws IOException {
    jsonWriter.sendError(response, HttpStatus.UNAUTHORIZED, "Unauthorized access");

    then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
  }

  // -------------------------------------------------------------------------
  // ErrorResponse body construction
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("sendError() writes an ErrorResponse with the correct status code")
  void sendError_writesErrorResponseWithCorrectStatusCode() throws IOException {
    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);

    jsonWriter.sendError(response, HttpStatus.NOT_FOUND, "Resource not found");

    then(objectMapper).should().writeValue(eq(outputStream), captor.capture());
    assertThat(captor.getValue()).hasFieldOrPropertyWithValue("code", HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("sendError() writes an ErrorResponse with the correct status name")
  void sendError_writesErrorResponseWithCorrectStatusName() throws IOException {
    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);

    jsonWriter.sendError(response, HttpStatus.NOT_FOUND, "Resource not found");

    then(objectMapper).should().writeValue(eq(outputStream), captor.capture());
    assertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("name", HttpStatus.NOT_FOUND.name());
  }

  @Test
  @DisplayName("sendError() writes an ErrorResponse with the correct description")
  void sendError_writesErrorResponseWithCorrectDescription() throws IOException {
    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);

    jsonWriter.sendError(response, HttpStatus.NOT_FOUND, "Resource not found");

    then(objectMapper).should().writeValue(eq(outputStream), captor.capture());
    assertThat(captor.getValue()).hasFieldOrPropertyWithValue("description", "Resource not found");
  }

  @Test
  @DisplayName("sendError() writes the ErrorResponse to the response output stream")
  void sendError_writesToResponseOutputStream() throws IOException {
    jsonWriter.sendError(response, HttpStatus.FORBIDDEN, "Access denied");

    then(objectMapper).should().writeValue(eq(outputStream), any(ErrorResponse.class));
  }

  // -------------------------------------------------------------------------
  // Different HTTP statuses
  // -------------------------------------------------------------------------

  @ParameterizedTest(name = "sendError() handles {0}")
  @MethodSource("httpStatusProvider")
  @DisplayName("sendError() correctly maps status code and name for common error statuses")
  void sendError_handlesVariousHttpStatuses(HttpStatus status) throws IOException {
    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);

    jsonWriter.sendError(response, status, "Some error");

    then(objectMapper).should().writeValue(eq(outputStream), captor.capture());
    assertThat(captor.getValue())
        .hasFieldOrPropertyWithValue("code", status.value())
        .hasFieldOrPropertyWithValue("name", status.name());
  }

  static Stream<HttpStatus> httpStatusProvider() {
    return Stream.of(
        HttpStatus.BAD_REQUEST,
        HttpStatus.UNAUTHORIZED,
        HttpStatus.FORBIDDEN,
        HttpStatus.NOT_FOUND,
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // -------------------------------------------------------------------------
  // IOException propagation
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("sendError() propagates IOException thrown by ObjectMapper")
  void sendError_propagatesIoExceptionFromObjectMapper() throws IOException {
    willThrow(new IOException("Stream closed"))
        .given(objectMapper)
        .writeValue(eq(outputStream), any(ErrorResponse.class));

    assertThatThrownBy(() -> jsonWriter.sendError(response, HttpStatus.UNAUTHORIZED, "error"))
        .isInstanceOf(IOException.class)
        .hasMessage("Stream closed");
  }

  @Test
  @DisplayName("sendError() propagates IOException thrown by getOutputStream()")
  void sendError_propagatesIoExceptionFromGetOutputStream() throws IOException {
    given(response.getOutputStream()).willThrow(new IOException("Output stream unavailable"));

    assertThatThrownBy(() -> jsonWriter.sendError(response, HttpStatus.UNAUTHORIZED, "error"))
        .isInstanceOf(IOException.class)
        .hasMessage("Output stream unavailable");
  }
}
