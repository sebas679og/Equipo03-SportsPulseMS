package com.sportspulse.leagues.utils.mappers;

import com.sportspulse.leagues.dto.responses.LeagueCurrentSeason;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummary;
import com.sportspulse.leagues.integration.football.dto.ApiResponse;
import com.sportspulse.leagues.integration.football.dto.ApiSeason;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * LeaguesMapper Mapper interface for converting API-Football payloads into leagues DTOs.
 *
 * <p>Uses MapStruct to generate mapping code and keeps season-selection logic centralized in helper
 * methods.
 */
@Mapper(componentModel = "spring")
public interface LeaguesMapper {

  default List<LeagueSummary> toSummaryList(List<ApiResponse> responses) {
    return responses.stream().map(this::toSummary).toList();
  }

  @Mapping(target = "id", source = "league.id")
  @Mapping(target = "name", source = "league.name")
  @Mapping(target = "type", source = "league.type")
  @Mapping(target = "country", source = "country.name")
  @Mapping(target = "logo", source = "league.logo")
  @Mapping(target = "currentSeason", expression = "java(findCurrentSeason(response).year())")
  @Mapping(target = "startDate", expression = "java(findCurrentSeason(response).start())")
  @Mapping(target = "endDate", expression = "java(findCurrentSeason(response).end())")
  LeagueSummary toSummary(ApiResponse response);

  @Mapping(target = "id", source = "league.id")
  @Mapping(target = "name", source = "league.name")
  @Mapping(target = "type", source = "league.type")
  @Mapping(target = "country", source = "country.name")
  @Mapping(target = "logo", source = "league.logo")
  @Mapping(target = "seasons", expression = "java(extractYears(response.seasons()))")
  @Mapping(
      target = "currentSeason",
      expression = "java(toCurrentSeason(findCurrentSeason(response)))")
  LeagueDetailResponse toDetail(ApiResponse response);

  @Mapping(target = "startDate", source = "start")
  @Mapping(target = "endDate", source = "end")
  LeagueCurrentSeason toCurrentSeason(ApiSeason season);

  default ApiSeason findCurrentSeason(ApiResponse response) {
    return response.seasons().stream()
        .filter(ApiSeason::current)
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "No current season for league: " + response.league().id()));
  }

  default List<Integer> extractYears(List<ApiSeason> seasons) {
    return seasons.stream().map(ApiSeason::year).toList();
  }
}
