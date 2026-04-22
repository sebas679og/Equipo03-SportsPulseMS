package com.sportspulse.teams.security.filter;

import com.sportspulse.teams.client.AuthClient;
import com.sportspulse.teams.constants.Errors;
import com.sportspulse.teams.constants.HttpHeaders;
import com.sportspulse.teams.dto.internal.ValidateResponse;
import com.sportspulse.teams.dto.response.ErrorResponse;
import com.sportspulse.teams.security.writer.ErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * OncePerRequestFilter that validates incoming JWT tokens against the authentication service.
 *
 * <p>Extracts the Bearer token from the {@code Authorization} header, delegates validation to
 * {@link AuthClient}, and populates the {@link SecurityContextHolder} on success. Returns a
 * structured {@link ErrorResponse} on failure.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ROLE_PREFIX = "ROLE_";

  private final AuthClient authClient;
  private final ErrorWriter errorWriter;

  /**
   * Validates the JWT token and sets the authentication in the security context.
   *
   * @param request the incoming HTTP request.
   * @param response the HTTP response.
   * @param filterChain the filter chain to continue if authentication succeeds.
   * @throws ServletException if a servlet error occurs.
   * @throws IOException if an I/O error occurs writing the error response.
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      errorWriter.write(
          response,
          HttpStatus.UNAUTHORIZED,
          Errors.Code.MISSING_TOKEN,
          Errors.Message.MISSING_TOKEN);
      return;
    }

    try {
      ValidateResponse validateResponse = authClient.validate(authHeader);

      if (!validateResponse.valid()) {
        errorWriter.write(
            response,
            HttpStatus.UNAUTHORIZED,
            Errors.Code.INVALID_TOKEN,
            Errors.Message.INVALID_TOKEN);
        return;
      }

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              validateResponse.username(),
              null,
              List.of(new SimpleGrantedAuthority(ROLE_PREFIX + validateResponse.role())));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);

    } catch (Exception e) {
      errorWriter.write(
          response,
          HttpStatus.UNAUTHORIZED,
          Errors.Code.TOKEN_VALIDATION_ERROR,
          Errors.Message.TOKEN_VALIDATION_ERROR);
    }
  }
}
