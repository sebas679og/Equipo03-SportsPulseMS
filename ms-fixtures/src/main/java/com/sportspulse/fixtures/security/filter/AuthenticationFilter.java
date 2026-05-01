package com.sportspulse.fixtures.security.filter;

import com.sportspulse.fixtures.client.AuthClient;
import com.sportspulse.fixtures.constants.HttpHeaders;
import com.sportspulse.fixtures.dto.internal.InternalUserResponse;
import com.sportspulse.fixtures.exceptions.BadGatewayException;
import com.sportspulse.fixtures.exceptions.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security filter that intercepts every incoming request to validate the Bearer token.
 *
 * <p>It extracts the JWT from the Authorization header and verifies it via the {@link AuthClient}.
 * If valid, it populates the {@link SecurityContextHolder} with the user's identity and
 * authorities, allowing downstream controllers to perform role-based access control (RBAC).
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

  private final AuthClient authClient;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.Auth.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith(HttpHeaders.Auth.BEARER)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(HttpHeaders.Auth.BEARER.length()).strip();

    try {
      InternalUserResponse user = authClient.isTokenValid(token);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              user,
              null,
              List.of(
                  buildRoleAuthority(user.role()),
                  new SimpleGrantedAuthority(HttpHeaders.Auth.ROLE_AUTH_JWT)));

      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (UnauthorizedException e) {
      log.warn("Invalid or expired token: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    } catch (BadGatewayException e) {
      log.error("ms-auth not available during token validation: {}", e.getMessage());
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Normalizes the role string into a Spring Security GrantedAuthority. e.g., "admin" ->
   * "ROLE_ADMIN"
   */
  private SimpleGrantedAuthority buildRoleAuthority(String role) {
    String normalized = (role != null) ? role.toUpperCase().strip() : HttpHeaders.Auth.ROLE_USER;
    return new SimpleGrantedAuthority(HttpHeaders.Auth.PREFIX_ROLE + normalized);
  }
}
