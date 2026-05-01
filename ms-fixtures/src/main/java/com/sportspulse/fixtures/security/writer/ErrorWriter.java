package com.sportspulse.fixtures.security.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.fixtures.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * Utility component to write standardized ErrorResponse objects directly to the HttpServletResponse
 * stream.
 *
 * <p>Crucial for Security Filters which operate outside the standard Spring MVC exception handling
 * mechanism. It ensures that 401 Unauthorized or 403 Forbidden errors maintain a consistent JSON
 * structure with the rest of the API.
 */
@Component
@RequiredArgsConstructor
public class ErrorWriter {
  private final ObjectMapper objectMapper;

  /**
   * Serializes an ErrorResponse record and writes it to the response body.
   *
   * @param response The servlet response to modify.
   * @param status The HTTP status to return.
   * @param message The descriptive error message.
   * @throws IOException If an error occurs during writing to the output stream.
   */
  public void sendError(HttpServletResponse response, HttpStatus status, String message)
      throws IOException {
    ErrorResponse body = ErrorResponse.builder().error(status.name()).message(message).build();

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
