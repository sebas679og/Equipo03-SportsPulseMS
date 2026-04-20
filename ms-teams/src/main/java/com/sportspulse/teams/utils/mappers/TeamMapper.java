package com.sportspulse.teams.utils.mappers;

import com.sportspulse.teams.dto.responses.StadiumByIdResponse;
import com.sportspulse.teams.dto.responses.TeamByIdResponse;
import com.sportspulse.teams.integration.football.dto.teamid.ApiResponseItem;
import com.sportspulse.teams.integration.football.dto.teamid.ApiVenueResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * TeamMapper Mapper interface for converting API responses into domain-specific models.
 *
 * <p>Uses MapStruct to automatically generate the implementation for mapping between external API
 * response objects and internal application DTOs.
 */
@Mapper(componentModel = "spring")
public interface TeamMapper {

  @Mapping(target = "stadium", source = "venue")
  @Mapping(target = "id", source = "team.id")
  @Mapping(target = "name", source = "team.name")
  @Mapping(target = "country", source = "team.country")
  @Mapping(target = "logo", source = "team.logo")
  @Mapping(target = "founded", source = "team.founded")
  @Mapping(target = "national", source = "team.national")
  TeamByIdResponse toTeamResponse(ApiResponseItem item);

  StadiumByIdResponse toStadiumResponse(ApiVenueResponse venue);
}
