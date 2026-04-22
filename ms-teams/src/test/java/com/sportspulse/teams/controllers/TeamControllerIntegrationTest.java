package com.sportspulse.teams.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sportspulse.teams.client.AuthClient;
import com.sportspulse.teams.config.OpenApiConfig;
import com.sportspulse.teams.config.SecurityConfig;
import com.sportspulse.teams.constants.ApiPaths;
import com.sportspulse.teams.constants.Errors;
import com.sportspulse.teams.constants.HttpHeaders;
import com.sportspulse.teams.dto.response.TeamResponse;
import com.sportspulse.teams.dto.response.VenueResponse;
import com.sportspulse.teams.exceptions.GlobalExceptionHandler;
import com.sportspulse.teams.security.filter.JwtAuthenticationFilter;
import com.sportspulse.teams.service.TeamService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TeamController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, GlobalExceptionHandler.class, OpenApiConfig.class})
class TeamControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TeamService teamService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockitoBean private AuthClient authClient;

  private static final String VALID_TOKEN = "Bearer valid.token";

  @BeforeEach
  void setupFilter() throws Exception {
    doAnswer(
            invocation -> {
              HttpServletRequest request = invocation.getArgument(0);
              HttpServletResponse response = invocation.getArgument(1);
              FilterChain chain = invocation.getArgument(2);

              String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

              if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response
                    .getWriter()
                    .write(
                        "{\"error\":\"MISSING_TOKEN\","
                            + "\"message\":\"Authorization header is missing or invalid.\","
                            + "\"timestamp\":\""
                            + Instant.now()
                            + "\"}");
                return null;
              }

              UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(
                      "javier_ruiz15", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
              SecurityContextHolder.getContext().setAuthentication(authentication);

              chain.doFilter(request, response);
              return null;
            })
        .when(jwtAuthenticationFilter)
        .doFilter(any(), any(), any());
  }

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  private List<TeamResponse> buildTeamResponseList() {
    return List.of(
        new TeamResponse(
            33,
            "Manchester United",
            "England",
            "https://logo.png",
            1878,
            new VenueResponse("Old Trafford", "Manchester", 76212)));
  }

  @Test
  void getTeams_shouldReturn401_whenAuthHeaderIsMissing() throws Exception {
    mockMvc
        .perform(get(ApiPaths.Team.BASE).param("league", "39").param("season", "2023"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value(Errors.Code.MISSING_TOKEN));
  }

  @Test
  void getTeams_shouldReturn400_whenLeagueParamIsMissing() throws Exception {
    mockMvc
        .perform(
            get(ApiPaths.Team.BASE)
                .header(HttpHeaders.AUTHORIZATION, VALID_TOKEN)
                .param("season", "2023"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(Errors.Code.MISSING_PARAMETER))
        .andExpect(jsonPath("$.message").value(containsString("league")));
  }

  @Test
  void getTeams_shouldReturn400_whenSeasonParamIsMissing() throws Exception {
    mockMvc
        .perform(
            get(ApiPaths.Team.BASE)
                .header(HttpHeaders.AUTHORIZATION, VALID_TOKEN)
                .param("league", "39"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(Errors.Code.MISSING_PARAMETER))
        .andExpect(jsonPath("$.message").value(containsString("season")));
  }

  @Test
  void getTeams_shouldReturn200_whenRequestIsValid() throws Exception {

    when(teamService.getTeams(39, 2023)).thenReturn(buildTeamResponseList());

    mockMvc
        .perform(
            get(ApiPaths.Team.BASE)
                .header(HttpHeaders.AUTHORIZATION, VALID_TOKEN)
                .param("league", "39")
                .param("season", "2023"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(33))
        .andExpect(jsonPath("$[0].name").value("Manchester United"))
        .andExpect(jsonPath("$[0].country").value("England"))
        .andExpect(jsonPath("$[0].logo").value("https://logo.png"))
        .andExpect(jsonPath("$[0].founded").value(1878))
        .andExpect(jsonPath("$[0].venue.name").value("Old Trafford"))
        .andExpect(jsonPath("$[0].venue.city").value("Manchester"))
        .andExpect(jsonPath("$[0].venue.capacity").value(76212));
  }
}
