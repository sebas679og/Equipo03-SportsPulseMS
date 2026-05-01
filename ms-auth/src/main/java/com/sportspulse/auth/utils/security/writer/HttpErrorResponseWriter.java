package com.sportspulse.auth.utils.security.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.auth.dto.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** HttpErrorResponseWriter Writes standardized error responses in JSON format. */
@Component
@RequiredArgsConstructor
public class HttpErrorResponseWriter {

  private final ObjectMapper objectMapper;

  /**
   * Writes an error response to the HTTP response stream.
   *
   * @param response the HttpServletResponse to write to
   * @param status the HTTP status to set
   * @param description a description of the error
   * @throws IOException if writing to the response fails
   */
  public void write(HttpServletResponse response, HttpStatus status, String description)
      throws IOException {

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(status.value())
            .name(status.getReasonPhrase())
            .description(description)
            .build();

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
