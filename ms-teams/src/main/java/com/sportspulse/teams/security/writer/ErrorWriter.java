package com.sportspulse.teams.security.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.teams.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** Handles writing and formatting error responses for the security layer. */
@Component
@RequiredArgsConstructor
public class ErrorWriter {
  private final ObjectMapper objectMapper;

  /**
   * Writes an {@link ErrorResponse} with the given HTTP status to the response.
   *
   * @param response the HTTP response to write to.
   * @param status the HTTP status code to set.
   * @param error the error code.
   * @param message the error message.
   * @throws IOException if an I/O error occurs during writing.
   */
  public void write(HttpServletResponse response, HttpStatus status, String error, String message)
      throws IOException {

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), ErrorResponse.of(error, message));
  }
}
