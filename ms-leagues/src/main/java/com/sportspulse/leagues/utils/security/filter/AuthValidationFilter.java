package com.sportspulse.leagues.utils.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.leagues.config.constants.ApiPaths;
import com.sportspulse.leagues.config.constants.InternalHeaders;
import com.sportspulse.leagues.dto.responses.LeagueErrorResponse;
import com.sportspulse.leagues.exceptions.CustomServiceUnavailableException;
import com.sportspulse.leagues.exceptions.CustomUnauthorizedException;
import com.sportspulse.leagues.integration.msauth.AuthClient;
import com.sportspulse.leagues.integration.msauth.dto.UserResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

@Component
@RequiredArgsConstructor
public class AuthValidationFilter extends OncePerRequestFilter {

  private final AuthClient authClient;
  private final ObjectMapper objectMapper;
  private final AntPathMatcher antPathMatcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authorization = request.getHeader(InternalHeaders.MsAuth.BEARER_HEADER);
    if (authorization == null
        || !authorization.startsWith(InternalHeaders.MsAuth.TYPE_TOKEN + " ")) {
      writeError(response, HttpStatus.UNAUTHORIZED, "Authorization header es requerido");
      return;
    }

    try {
      String token = authorization.substring(InternalHeaders.MsAuth.TYPE_TOKEN.length() + 1);
      UserResponse validationResponse = authClient.isTokenValid(token);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
            validationResponse.username(),
              null,
            List.of(new SimpleGrantedAuthority("ROLE_" + validationResponse.role())));
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      filterChain.doFilter(request, response);
    } catch (CustomUnauthorizedException ex) {
      SecurityContextHolder.clearContext();
      writeError(
          response,
          HttpStatus.UNAUTHORIZED,
          "Token de autenticación inválido o ausente");
    } catch (CustomServiceUnavailableException ex) {
      SecurityContextHolder.clearContext();
      writeError(
          response,
          HttpStatus.SERVICE_UNAVAILABLE,
          "No se pudo validar el token con ms-auth");
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

  private void writeError(HttpServletResponse response, HttpStatus status, String description)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    LeagueErrorResponse body =
        LeagueErrorResponse.builder()
            .code(status.value())
            .name(status.getReasonPhrase())
            .description(description)
            .build();
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}