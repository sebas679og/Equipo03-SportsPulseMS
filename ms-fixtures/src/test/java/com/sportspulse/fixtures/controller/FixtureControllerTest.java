package com.sportspulse.fixtures.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sportspulse.fixtures.constants.ApiPaths;
import com.sportspulse.fixtures.service.FixtureService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = FixtureController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      UserDetailsServiceAutoConfiguration.class
    })
@AutoConfigureMockMvc(addFilters = false)
class FixtureControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private FixtureService fixtureService;

  @Test
  void getFixtures_ShouldReturnOk() throws Exception {
    // GIVEN
    when(fixtureService.getFixtures(any())).thenReturn(List.of());

    // WHEN & THEN
    mockMvc
        .perform(get(ApiPaths.Fixtures.FIXTURES).param("league", "1").param("date", "2024-05-20"))
        .andExpect(status().isOk());
  }

  @Test
  void getFixtureEvents_ShouldReturnEventsList() throws Exception {

    Long fixtureId = 123L;

    when(fixtureService.getFixtureEvents(fixtureId)).thenReturn(List.of());

    String url = ApiPaths.Fixtures.FIXTURE_EVENTS.replace("{fixtureId}", fixtureId.toString());

    mockMvc.perform(get(url)).andExpect(status().isOk());
  }

  @Test
  void getLiveFixtures_ShouldReturnOk() throws Exception {

    when(fixtureService.getLiveFixtures()).thenReturn(List.of());

    mockMvc
        .perform(get(ApiPaths.Fixtures.FIXTURE_LIVE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }
}
