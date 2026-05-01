package com.sportspulse.fixtures.service;

import com.sportspulse.fixtures.client.FootballClient;
import com.sportspulse.fixtures.dto.request.FixtureFilterRequest;
import com.sportspulse.fixtures.dto.response.FixtureResponse;
import com.sportspulse.fixtures.dto.response.event.FixtureEventResponse;
import com.sportspulse.fixtures.dto.response.live.FixtureLiveResponse;
import com.sportspulse.fixtures.mapper.FixtureMapper;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business service responsible for managing fixture data retrieval.
 *
 * <p>This service coordinates with the {@link FootballClient} to fetch data from upstream providers
 * and utilizes the {@link FixtureMapper} to transform the results into sanitized DTOs. It ensures
 * that incoming requests have sensible defaults, such as using the current date if no date is
 * provided.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixtureService {

  private final FootballClient footballClient;
  private final FixtureMapper fixtureMapper;

  /**
   * Retrieves a list of fixtures filtered by the provided criteria.
   *
   * @param request The filter parameters (league, team, date, status).
   * @return A list of mapped {@link FixtureResponse} objects.
   */
  public List<FixtureResponse> getFixtures(FixtureFilterRequest request) {

    if (request.date() == null) {
      request =
          new FixtureFilterRequest(
              request.league(), request.team(), LocalDate.now().toString(), request.status());
    }

    log.info(
        "FixtureService - Request to API-Football: league={}, team={}, date={}, status={}",
        request.league(),
        request.team(),
        request.date(),
        request.status());

    return footballClient.getFixtures(request).stream()
        .map(fixtureMapper::toFixtureResponse)
        .toList();
  }

  /**
   * Retrieves all currently active (live) fixtures.
   *
   * @return A list of {@link FixtureLiveResponse} containing real-time match data.
   */
  public List<FixtureLiveResponse> getLiveFixtures() {

    return footballClient.getFixtureLive().stream()
        .map(fixtureMapper::toLiveFixtureResponse)
        .toList();
  }

  /**
   * Retrieves a detailed timeline of events for a specific fixture.
   *
   * @param fixtureId The unique identifier of the fixture.
   * @return A list of {@link FixtureEventResponse} items (goals, cards, substitutions).
   */
  public List<FixtureEventResponse> getFixtureEvents(Long fixtureId) {
    return footballClient.getFixtureEvents(fixtureId).stream()
        .map(fixtureMapper::toFixtureEventResponse)
        .toList();
  }
}
