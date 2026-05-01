package com.sportspulse.fixtures.security.filter;

import com.sportspulse.fixtures.config.properties.AuthProperties;
import com.sportspulse.fixtures.constants.HttpHeaders;
import com.sportspulse.fixtures.security.writer.ErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Security filter for Service-to-Service (S2S) authentication.
 *
 * <p>Validates an internal API key provided in the headers. This is used by trusted internal
 * services to perform administrative or automated tasks without requiring a per-user JWT.
 */
@Slf4j
@RequiredArgsConstructor
public class InternalKeyAuthenticationFilter extends OncePerRequestFilter {

  private final AuthProperties authProperties;
  private final ErrorWriter writer;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String apiKey = request.getHeader(HttpHeaders.Auth.AUTH_INTERNAL_KEY);

    if (apiKey == null) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!MessageDigest.isEqual(authProperties.getApiKey().getBytes(), apiKey.getBytes())) {
      log.warn("Invalid X-Internal-API-Key from IP: {}", getClientIp(request));
      writer.sendError(response, HttpStatus.UNAUTHORIZED, "Invalid internal API key");
      return;
    }

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "internal-service",
            null,
            List.of(new SimpleGrantedAuthority(HttpHeaders.Auth.ROLE_AUTH_INTERNAL)));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }

  /** Helper to resolve the real IP, considering potential proxies/load balancers. */
  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return (forwarded != null) ? forwarded.split(",")[0].strip() : request.getRemoteAddr();
  }
}
