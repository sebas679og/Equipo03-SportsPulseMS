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
   * <p>Maps all responses without filtering. The API already returns only the leagues matching the
   * requested season, so no additional filtering is needed here.
   *
   * @param responses the list of API responses to process
   * @return a list of league summaries
   */
  default List<LeagueSummary> toSummaryList(List<ApiResponse> responses) {
    return responses.stream().map(this::toSummary).toList();
  }

  @Mapping(target = "id", source = "league.id")
  @Mapping(target = "name", source = "league.name")
  @Mapping(target = "type", source = "league.type")
  @Mapping(target = "country", source = "country.name")
  @Mapping(target = "logo", source = "league.logo")
  @Mapping(target = "currentSeason", expression = "java(firstSeason(response).year())")
  @Mapping(target = "startDate", expression = "java(firstSeason(response).start())")
  @Mapping(target = "endDate", expression = "java(firstSeason(response).end())")
  LeagueSummary toSummary(ApiResponse response);

  @Mapping(target = "id", source = "league.id")
  @Mapping(target = "name", source = "league.name")
  @Mapping(target = "type", source = "league.type")
  @Mapping(target = "country", source = "country.name")
  @Mapping(target = "logo", source = "league.logo")
  @Mapping(target = "seasons", expression = "java(extractYears(response.seasons()))")
  @Mapping(
      target = "currentSeason",
      expression = "java(toLeagueCurrentSeason(firstSeason(response)))")
  LeagueDetailResponse toDetail(ApiResponse response);

  @Mapping(target = "startDate", source = "start")
  @Mapping(target = "endDate", source = "end")
  LeagueCurrentSeason toLeagueCurrentSeason(ApiSeason season);

  /**
   * Returns the first season from the given {@link ApiResponse}.
   *
   * <p>When the API is queried by season, each response contains exactly one season in the {@code
   * seasons} array — the one matching the requested year. This method retrieves it without any
   * filtering.
   *
   * @param response the API response containing league and season information
   * @return the first (and typically only) season in the response
   * @throws ExternalDataInconsistencyException if the seasons list is empty
   */
  default ApiSeason firstSeason(ApiResponse response) {
    return response.seasons().stream()
        .findFirst()
        .orElseThrow(
            () ->
                new ExternalDataInconsistencyException(
                    "No season data for league: " + response.league().id()));
  }

  default List<Integer> extractYears(List<ApiSeason> seasons) {
    return seasons.stream().map(ApiSeason::year).toList();
  }
}
