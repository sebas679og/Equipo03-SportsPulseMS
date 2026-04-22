package com.sportspulse.teams.filters;

import com.sportspulse.teams.config.constants.InternalHeaders;
import com.sportspulse.teams.config.properties.MsAuthProperties;
import com.sportspulse.teams.exceptions.JsonWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * InternalApiKeyAuthenticationFilter Custom authentication filter that validates internal API key
 * requests.
 *
 * <p>Extracts the API key from the {@code X-Internal-API-Key} header and compares it against the
 * configured value in {@link MsAuthProperties}. If the key is missing, the request continues
 * without authentication. If the key is invalid, an {@code UNAUTHORIZED} error response is sent
 * using {@link JsonWriter}.
 *
 * <p>When the key is valid, an {@link UsernamePasswordAuthenticationToken} with {@code
 * ROLE_INTERNAL} authority is set in the {@link SecurityContextHolder}.
 */
@Slf4j
@RequiredArgsConstructor
public class InternalApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private final MsAuthProperties securityProperties;
  private final JsonWriter writer;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String apiKey = request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY);

    if (apiKey == null) {
      filterChain.doFilter(request, response);
      return;
    }

    if (!securityProperties.getApiKey().equals(apiKey)) {
      if (log.isWarnEnabled()) {
        log.warn("X-Internal-API-Key inválida desde IP: {}", request.getRemoteAddr());
      }
      writer.sendError(response, HttpStatus.UNAUTHORIZED, "Invalid internal API key");
      return;
    }

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            "internal-service", null, List.of(new SimpleGrantedAuthority("AUTH_INTERNAL")));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }
}
