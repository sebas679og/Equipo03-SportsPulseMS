package com.sportspulse.auth.utils.security.filter;

import com.sportspulse.auth.config.constants.ApiPaths;
import com.sportspulse.auth.utils.security.extractor.InternalApiKeyExtractor;
import com.sportspulse.auth.utils.security.writer.HttpErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** InternalApiKeyFilter Filters incoming requests to validate the internal API key. */
@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

  private final InternalApiKeyExtractor apiKeyExtractor;
  private final HttpErrorResponseWriter errorResponseWriter;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (!apiKeyExtractor.isValid(request)) {
      errorResponseWriter.write(
          response, HttpStatus.UNAUTHORIZED, "Invalid or missing internal API key");
      return;
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().equals(ApiPaths.Validate.TOKEN);
  }
}
