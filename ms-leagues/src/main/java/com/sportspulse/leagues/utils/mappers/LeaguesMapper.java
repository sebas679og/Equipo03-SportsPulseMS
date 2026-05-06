package com.sportspulse.leagues.utils.mappers;

import com.sportspulse.leagues.dto.responses.LeagueCurrentSeason;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummary;
import com.sportspulse.leagues.exceptions.ExternalDataInconsistencyException;
import com.sportspulse.leagues.integrations.football.dto.ApiResponse;
import com.sportspulse.leagues.integrations.football.dto.ApiSeason;
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

  /**
   * Converts a list of {@link ApiResponse} objects into a list of {@link LeagueSummary} instances.
   *
   * <p>Only includes responses that contain at least one current season. Each valid response is
   * mapped to its corresponding summary representation using {@link #toSummary(ApiResponse)}.
   *
   * @param responses the list of API responses to process
   * @return a list of league summaries for responses with a current season
   */
  default List<LeagueSummary> toSummaryList(List<ApiResponse> responses) {
    return responses.stream()
        .filter(r -> r.seasons().stream().anyMatch(ApiSeason::current))
        .map(this::toSummary)
        .toList();
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
      expression = "java(toLeagueCurrentSeason(findCurrentSeason(response)))")
  LeagueDetailResponse toDetail(ApiResponse response);

  @Mapping(target = "startDate", source = "start")
  @Mapping(target = "endDate", source = "end")
  LeagueCurrentSeason toLeagueCurrentSeason(ApiSeason season);

  /**
   * Finds the current season from the given {@link ApiResponse}.
   *
   * <p>Searches through the list of {@link ApiSeason} objects associated with the response and
   * returns the first one marked as current. If no current season is found, an {@link
   * ExternalDataInconsistencyException} is thrown to indicate inconsistent or invalid external
   * data.
   *
   * @param response the API response containing league and season information
   * @return the current season associated with the league
   * @throws ExternalDataInconsistencyException if no current season exists for the league
   */
  default ApiSeason findCurrentSeason(ApiResponse response) {
    return response.seasons().stream()
        .filter(ApiSeason::current)
        .findFirst()
        .orElseThrow(
            () ->
                new ExternalDataInconsistencyException(
                    "No current season for league: " + response.league().id()));
  }

  default List<Integer> extractYears(List<ApiSeason> seasons) {
    return seasons.stream().map(ApiSeason::year).toList();
  }
}
