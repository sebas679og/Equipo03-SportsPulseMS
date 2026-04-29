package com.sportspulse.leagues.services;

import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummaryResponse;
import com.sportspulse.leagues.exceptions.LeagueNotFoundException;
import com.sportspulse.leagues.integration.football.FootballApiClient;
import com.sportspulse.leagues.utils.mappers.LeaguesMapper;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Default leagues service implementation. */
@Service
@RequiredArgsConstructor
public class LeaguesServiceImpl implements LeaguesService {

  private final FootballApiClient footballApiClient;
  private final LeaguesMapper leaguesMapper;

  @Override
  public List<LeagueSummaryResponse> getLeagues(String country, Integer season) {
    String normalizedCountry = StringUtils.hasText(country) ? country.trim() : null;

    return footballApiClient.getLeagues(normalizedCountry, season, null).stream()
        .map(league -> leaguesMapper.toLeagueSummaryResponse(league, season))
        .toList();
  }

  @Override
  public LeagueDetailResponse getLeagueById(Integer leagueId) {
    return footballApiClient.getLeagues(null, null, leagueId).stream()
        .filter(item -> Objects.equals(item.getLeague().getId(), leagueId))
        .findFirst()
        .map(leaguesMapper::toLeagueDetailResponse)
        .orElseThrow(LeagueNotFoundException::new);
  }
}
