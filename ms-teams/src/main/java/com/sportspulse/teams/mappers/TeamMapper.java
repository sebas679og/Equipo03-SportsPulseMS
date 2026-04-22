package com.sportspulse.teams.mappers;

import com.sportspulse.teams.dto.external.TeamVenueWrapper;
import com.sportspulse.teams.dto.external.VenueData;
import com.sportspulse.teams.dto.response.TeamResponse;
import com.sportspulse.teams.dto.response.VenueResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface TeamMapper {
    /**
     * Maps a {@link TeamVenueWrapper} from API-Football to a {@link TeamResponse}.
     *
     * @param wrapper the raw team and venue data.
     * @return a mapped {@link TeamResponse}.
     */
    @Mapping(source = "team.id", target = "id")
    @Mapping(source = "team.name", target = "name")
    @Mapping(source = "team.country", target = "country")
    @Mapping(source = "team.logo", target = "logo")
    @Mapping(source = "team.founded", target = "founded")
    @Mapping(source = "venue", target = "venue")
    TeamResponse toTeamResponse(TeamVenueWrapper wrapper);

    /**
     * Maps a {@link VenueData} to a {@link VenueResponse}.
     *
     * @param venueData the raw venue data.
     * @return a mapped {@link VenueResponse}.
     */
    VenueResponse toVenue(VenueData venueData);
}
