package com.sportspulse.standings.utils.mappers;

import com.sportspulse.standings.dtos.responses.League;
import com.sportspulse.standings.dtos.responses.Standing;
import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;
import com.sportspulse.standings.dtos.responses.Team;
import com.sportspulse.standings.integrations.football.dto.ApiLeague;
import com.sportspulse.standings.integrations.football.dto.ApiStanding;
import com.sportspulse.standings.integrations.football.dto.ApiStandingsResponse;
import com.sportspulse.standings.integrations.football.dto.ApiTeam;
import java.util.Collections;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * StandingsMapper MapStruct mapper interface for converting API response objects into internal DTOs
 * related to league standings.
 *
 * <p>Provides mapping definitions between external API models (e.g., {@code ApiStandingsResponse},
 * {@code ApiLeague}, {@code ApiTeam}, {@code ApiStanding}) and internal domain objects ({@link
 * StandingsLeagueAndSeasonResponse}, {@link League}, {@link Team}, {@link Standing}).
 *
 * <p>Uses MapStruct annotations to define field-level mappings and includes a default method to
 * flatten nested standings lists.
 */
@Mapper(componentModel = "spring")
public interface StandingsMapper {

  @Mapping(target = "league", source = "response[0].league")
  @Mapping(
      target = "standings",
      source = "response[0].league.standings",
      qualifiedByName = "mapStandings")
  StandingsLeagueAndSeasonResponse toResponse(ApiStandingsResponse apiResponse);

  League toLeague(ApiLeague apiLeague);

  Team toTeam(ApiTeam apiTeam);

  @Mapping(target = "goalDifference", source = "goalsDiff")
  @Mapping(target = "played", source = "all.played")
  @Mapping(target = "won", source = "all.win")
  @Mapping(target = "drawn", source = "all.draw")
  @Mapping(target = "lost", source = "all.lose")
  @Mapping(target = "goalsFor", source = "all.goals.goalsFor")
  @Mapping(target = "goalsAgainst", source = "all.goals.against")
  Standing toStanding(ApiStanding apiStanding);

  /**
   * Flattens nested lists of {@link ApiStanding} into a single list of {@link Standing}.
   *
   * <p>If the input is null or empty, returns an empty list.
   *
   * @param standings nested lists of API standings
   * @return a flattened list of mapped {@link Standing} objects
   */
  @Named("mapStandings")
  default List<Standing> mapStandings(List<List<ApiStanding>> standings) {
    if (standings == null || standings.isEmpty()) {
      return Collections.emptyList();
    }

    return standings.stream().flatMap(List::stream).map(this::toStanding).toList();
  }
}
