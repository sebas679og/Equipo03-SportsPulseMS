package com.sportspulse.leagues.utils.mappers;

import com.sportspulse.leagues.dto.responses.LeagueCurrentSeasonResponse;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummaryResponse;
import com.sportspulse.leagues.integration.dto.ApiFootballLeagueWrapper;
import com.sportspulse.leagues.integration.dto.ApiFootballSeason;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * LeaguesMapper Mapper interface for converting API-Football payloads into leagues DTOs.
 *
 * <p>Uses MapStruct to generate mapping code and keeps season-selection logic centralized in
 * helper methods.
 */
@Mapper(componentModel = "spring")
public interface LeaguesMapper {

  @Mapping(target = "id", source = "item.league.id")
  @Mapping(target = "name", source = "item.league.name")
  @Mapping(target = "type", source = "item.league.type")
  @Mapping(target = "country", source = "item.country.name")
  @Mapping(target = "logo", source = "item.league.logo")
  @Mapping(target = "currentSeason", expression = "java(mapSeasonYear(item.getSeasons(), season))")
  @Mapping(target = "startDate", expression = "java(mapSeasonStart(item.getSeasons(), season))")
  @Mapping(target = "endDate", expression = "java(mapSeasonEnd(item.getSeasons(), season))")
  LeagueSummaryResponse toLeagueSummaryResponse(ApiFootballLeagueWrapper item, Integer season);

  @Mapping(target = "id", source = "item.league.id")
  @Mapping(target = "name", source = "item.league.name")
  @Mapping(target = "type", source = "item.league.type")
  @Mapping(target = "country", source = "item.country.name")
  @Mapping(target = "logo", source = "item.league.logo")
  @Mapping(target = "seasons", expression = "java(mapSeasonYears(item.getSeasons()))")
  @Mapping(target = "currentSeason", expression = "java(mapCurrentSeason(item.getSeasons()))")
  LeagueDetailResponse toLeagueDetailResponse(ApiFootballLeagueWrapper item);

  default LeagueCurrentSeasonResponse mapCurrentSeason(List<ApiFootballSeason> seasons) {
    return toCurrentSeasonResponse(resolveSeason(seasons, null));
  }

  default LeagueCurrentSeasonResponse toCurrentSeasonResponse(ApiFootballSeason season) {
    if (season == null) {
      return null;
    }
    return new LeagueCurrentSeasonResponse(
        season.getYear(), season.getStart(), season.getEnd(), season.getCurrent());
  }

  default List<Integer> mapSeasonYears(List<ApiFootballSeason> seasons) {
    return Optional.ofNullable(seasons).orElse(List.of()).stream()
        .map(ApiFootballSeason::getYear)
        .filter(Objects::nonNull)
        .sorted()
        .toList();
  }

  default Integer mapSeasonYear(List<ApiFootballSeason> seasons, Integer requestedYear) {
    ApiFootballSeason season = resolveSeason(seasons, requestedYear);
    return season != null ? season.getYear() : null;
  }

  default String mapSeasonStart(List<ApiFootballSeason> seasons, Integer requestedYear) {
    ApiFootballSeason season = resolveSeason(seasons, requestedYear);
    return season != null ? season.getStart() : null;
  }

  default String mapSeasonEnd(List<ApiFootballSeason> seasons, Integer requestedYear) {
    ApiFootballSeason season = resolveSeason(seasons, requestedYear);
    return season != null ? season.getEnd() : null;
  }

  default ApiFootballSeason resolveSeason(List<ApiFootballSeason> seasons, Integer year) {
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
