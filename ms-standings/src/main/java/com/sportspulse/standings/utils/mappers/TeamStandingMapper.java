package com.sportspulse.standings.utils.mappers;

import com.sportspulse.standings.dtos.responses.TeamStandingLeagueAndSeasonResponse;
import com.sportspulse.standings.integrations.football.dto.ApiLeague;
import com.sportspulse.standings.integrations.football.dto.ApiStanding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * TeamStandingMapper MapStruct mapper interface for converting API standing and league objects into
 * the internal {@link TeamStandingLeagueAndSeasonResponse} DTO.
 *
 * <p>Defines mapping rules between external API models ({@code ApiStanding}, {@code ApiLeague}) and
 * internal domain objects, ensuring structured transformation of team and league data into a
 * unified response format.
 *
 * <p>Configured as a Spring-managed component using {@code componentModel = "spring"}.
 */
@Mapper(componentModel = "spring")
public interface TeamStandingMapper {

  @Mapping(target = "team.id", source = "apiStanding.team.id")
  @Mapping(target = "team.name", source = "apiStanding.team.name")
  @Mapping(target = "league.id", source = "apiLeague.id")
  @Mapping(target = "league.name", source = "apiLeague.name")
  @Mapping(target = "season", source = "apiLeague.season")
  @Mapping(target = "rank", source = "apiStanding.rank")
  @Mapping(target = "points", source = "apiStanding.points")
  @Mapping(target = "played", source = "apiStanding.all.played")
  @Mapping(target = "form", source = "apiStanding.form")
  @Mapping(target = "description", source = "apiStanding.description")
  TeamStandingLeagueAndSeasonResponse toResponse(ApiStanding apiStanding, ApiLeague apiLeague);
}
