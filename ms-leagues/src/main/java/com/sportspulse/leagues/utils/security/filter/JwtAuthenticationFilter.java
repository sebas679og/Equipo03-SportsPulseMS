package com.sportspulse.leagues.utils.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.leagues.config.constants.ApiPaths;
import com.sportspulse.leagues.config.constants.InternalHeaders;
import com.sportspulse.leagues.config.properties.JwtProperties;
import com.sportspulse.leagues.dto.responses.LeagueErrorResponse;
import com.sportspulse.leagues.services.components.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/** Filter that validates incoming JWT tokens. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenService jwtTokenService;
  private final JwtProperties jwtProperties;
  private final ObjectMapper objectMapper;
  private final AntPathMatcher antPathMatcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authorization = request.getHeader(InternalHeaders.AUTHORIZATION);
    if (authorization == null || !authorization.startsWith(jwtProperties.getTokenType() + " ")) {
      writeUnauthorized(response);
      return;
    }

    String token = authorization.substring(jwtProperties.getTokenType().length() + 1);

    try {
      Claims claims = jwtTokenService.validateAndExtract(token);
      String username = claims.get("username", String.class);
      String role = claims.get("role", String.class);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (JwtException | IllegalArgumentException ex) {
      SecurityContextHolder.clearContext();
      writeUnauthorized(response);
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return antPathMatcher.match(ApiPaths.Docs.SWAGGER_UI, path)
        || antPathMatcher.match(ApiPaths.Docs.API_DOCS, path)
        || antPathMatcher.match(ApiPaths.State.HEALTH, path)
        || antPathMatcher.match("/error", path);
  }

  private void writeUnauthorized(HttpServletResponse response) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    LeagueErrorResponse body =
        new LeagueErrorResponse("UNAUTHORIZED", "Token JWT inválido o ausente", Instant.now());
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
