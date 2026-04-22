package com.sportspulse.leagues.services;

import com.sportspulse.leagues.config.CacheConfig;
import com.sportspulse.leagues.dto.responses.LeagueCurrentSeasonResponse;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummaryResponse;
import com.sportspulse.leagues.exceptions.LeagueNotFoundException;
import com.sportspulse.leagues.integration.client.FootballApiClient;
import com.sportspulse.leagues.integration.dto.ApiFootballLeagueWrapper;
import com.sportspulse.leagues.integration.dto.ApiFootballSeason;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** Default leagues service implementation. */
@Service
@RequiredArgsConstructor
public class LeaguesServiceImpl implements LeaguesService {

  private final FootballApiClient footballApiClient;

  @Override
  @Cacheable(
      value = CacheConfig.LEAGUES_BY_FILTERS_CACHE,
      key = "T(String).valueOf(#country) + ':' + T(String).valueOf(#season)")
  public List<LeagueSummaryResponse> getLeagues(String country, Integer season) {
    String normalizedCountry = StringUtils.hasText(country) ? country.trim() : null;

    return footballApiClient.getLeagues(normalizedCountry, season, null).stream()
        .map(league -> toSummaryResponse(league, season))
        .toList();
  }

  @Override
  @Cacheable(value = CacheConfig.LEAGUE_BY_ID_CACHE, key = "#leagueId")
  public LeagueDetailResponse getLeagueById(Integer leagueId) {
    return footballApiClient.getLeagues(null, null, leagueId).stream()
      .filter(item -> Objects.equals(item.getLeague().getId(), leagueId))
        .findFirst()
        .map(this::toDetailResponse)
        .orElseThrow(LeagueNotFoundException::new);
  }

  private LeagueSummaryResponse toSummaryResponse(ApiFootballLeagueWrapper item, Integer season) {
    ApiFootballSeason targetSeason = resolveSeason(item.getSeasons(), season);
    return new LeagueSummaryResponse(
      item.getLeague().getId(),
      item.getLeague().getName(),
      item.getLeague().getType(),
      item.getCountry() != null ? item.getCountry().getName() : null,
      item.getLeague().getLogo(),
      targetSeason != null ? targetSeason.getYear() : null,
      targetSeason != null ? targetSeason.getStart() : null,
      targetSeason != null ? targetSeason.getEnd() : null);
  }

  private LeagueDetailResponse toDetailResponse(ApiFootballLeagueWrapper item) {
    List<Integer> seasons =
      Optional.ofNullable(item.getSeasons()).orElse(List.of()).stream()
        .map(ApiFootballSeason::getYear)
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());

    ApiFootballSeason current = resolveSeason(item.getSeasons(), null);
    LeagueCurrentSeasonResponse currentSeason =
        current == null
            ? null
            : new LeagueCurrentSeasonResponse(
          current.getYear(), current.getStart(), current.getEnd(), current.getCurrent());

    return new LeagueDetailResponse(
      item.getLeague().getId(),
      item.getLeague().getName(),
      item.getLeague().getType(),
      item.getCountry() != null ? item.getCountry().getName() : null,
      item.getLeague().getLogo(),
        seasons,
        currentSeason);
  }

  private ApiFootballSeason resolveSeason(List<ApiFootballSeason> seasons, Integer year) {
    if (seasons == null || seasons.isEmpty()) {
      return null;
    }

    if (year != null) {
      Optional<ApiFootballSeason> selectedByYear =
          seasons.stream().filter(season -> Objects.equals(season.getYear(), year)).findFirst();
      if (selectedByYear.isPresent()) {
        return selectedByYear.get();
      }
    }

    return seasons.stream()
        .filter(season -> Boolean.TRUE.equals(season.getCurrent()))
        .findFirst()
        .orElseGet(
            () ->
                seasons.stream()
                    .filter(season -> season.getYear() != null)
                    .max(Comparator.comparing(ApiFootballSeason::getYear))
                    .orElse(null));
  }
}
