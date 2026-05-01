package com.sportspulse.auth.utils.security.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.auth.dto.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
@DisplayName("HttpErrorResponseWriter")
class HttpErrorResponseWriterTest {

  @Mock private ObjectMapper objectMapper;
  @Mock private HttpServletResponse response;

  @InjectMocks private HttpErrorResponseWriter writer;

  // ---------------------------------------------------------------------------
  // write() — happy path
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("write() sets the correct HTTP status code on the response")
  void write_setsCorrectStatusCode() throws IOException {
    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn("{}");

    writer.write(response, HttpStatus.UNAUTHORIZED, "Invalid or missing internal API key");

    verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("write() sets Content-Type to application/json")
  void write_setsJsonContentType() throws IOException {
    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn("{}");

    writer.write(response, HttpStatus.UNAUTHORIZED, "Invalid or missing internal API key");

    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
  }

  @Test
  @DisplayName("write() serializes an ErrorResponse built from the given status and description")
  void write_serializesCorrectErrorResponse() throws IOException {
    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);
    when(objectMapper.writeValueAsString(captor.capture())).thenReturn("{}");

    writer.write(response, HttpStatus.UNAUTHORIZED, "Invalid or missing internal API key");

    ErrorResponse captured = captor.getValue();
    assertThat(captured.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(captured.getName()).isEqualTo(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    assertThat(captured.getDescription()).isEqualTo("Invalid or missing internal API key");
  }

  @Test
  @DisplayName("write() writes the serialized JSON string to the response writer")
  void write_writesSerializedJsonToResponseWriter() throws IOException {
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);
    when(objectMapper.writeValueAsString(any(ErrorResponse.class))).thenReturn("{\"code\":401}");

    writer.write(response, HttpStatus.UNAUTHORIZED, "Invalid or missing internal API key");

    assertThat(stringWriter.toString()).isEqualTo("{\"code\":401}");
  }

  @Test
  @DisplayName("write() works correctly for different HTTP statuses")
  void write_handlesArbitraryHttpStatus() throws IOException {
    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);
    when(objectMapper.writeValueAsString(captor.capture())).thenReturn("{}");

    writer.write(response, HttpStatus.FORBIDDEN, "Access denied");

    ErrorResponse captured = captor.getValue();
    assertThat(captured.getCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(captured.getName()).isEqualTo(HttpStatus.FORBIDDEN.getReasonPhrase());
    assertThat(captured.getDescription()).isEqualTo("Access denied");
    verify(response).setStatus(HttpStatus.FORBIDDEN.value());
  }

  // ---------------------------------------------------------------------------
  // write() — error path
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("write() propagates IOException when ObjectMapper fails to serialize")
  void write_propagatesException_whenSerializationFails() throws IOException {
    when(objectMapper.writeValueAsString(any(ErrorResponse.class)))
        .thenThrow(new JsonProcessingException("serialization failed") {});

    assertThatThrownBy(() -> writer.write(response, HttpStatus.UNAUTHORIZED, "Invalid API key"))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("serialization failed");
  }
}
