package com.sportspulse.teams.filters;

import com.sportspulse.teams.exceptions.CustomUnauthorizedException;
import com.sportspulse.teams.integration.msauth.AuthClient;
import com.sportspulse.teams.integration.msauth.dto.UserResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * BearerAuthenticationFilter Custom authentication filter that processes Bearer tokens from
 * incoming requests.
 *
 * <p>Extracts the token from the {@code Authorization} header, validates it using the {@link
 * AuthClient}, and sets the authenticated user in the {@link SecurityContextHolder}. If the header
 * is missing or invalid, the request continues without authentication.
 */
@Slf4j
@RequiredArgsConstructor
public class BearerAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final AuthClient authClient;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(BEARER_PREFIX.length());

    UserResponse user = authClient.isTokenValid(token);

    try {
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              user,
              null,
              List.of(new SimpleGrantedAuthority(String.join("", "ROLE_", user.role()))));

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (CustomUnauthorizedException ignored) {
    }
  }
}
