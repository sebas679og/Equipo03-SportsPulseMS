package com.sportspulse.fixtures.utils.mappers;

import com.sportspulse.fixtures.dtos.responses.fixtures.Fixture;
import com.sportspulse.fixtures.dtos.responses.fixtures.League;
import com.sportspulse.fixtures.dtos.responses.fixtures.Status;
import com.sportspulse.fixtures.dtos.responses.fixtures.Team;
import com.sportspulse.fixtures.dtos.responses.fixtures.Venue;
import com.sportspulse.fixtures.integration.football.dto.ApiFixtureData;
import com.sportspulse.fixtures.integration.football.dto.ApiLeague;
import com.sportspulse.fixtures.integration.football.dto.ApiStatus;
import com.sportspulse.fixtures.integration.football.dto.ApiTeamInfo;
import com.sportspulse.fixtures.integration.football.dto.ApiVenue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FixtureMapper {

    @Mapping(target = "id", source = "fixture.id")
    @Mapping(target = "date", source = "fixture.date")
    @Mapping(target = "status", source = "fixture.status")
    @Mapping(target = "league", source = "league")
    @Mapping(target = "venue", source = "fixture.venue")
    @Mapping(target = "homeTeam", expression = "java(toTeam(api.teams().home(), api.goals().home()))")
    @Mapping(target = "awayTeam", expression = "java(toTeam(api.teams().away(), api.goals().away()))")
    Fixture toFixture(ApiFixtureData api);

    default Team toTeam(ApiTeamInfo teamInfo, Integer goals) {
        if (teamInfo == null) {
            return null;
        }
        return Team.builder()
                .id(teamInfo.id())
                .name(teamInfo.name())
                .logo(teamInfo.logo())
                .goals(goals)
                .build();
    }

    @Mapping(target = "shortName", source = "shortName")
    @Mapping(target = "longName", source = "longName")
    Status toStatus(ApiStatus status);

    League toLeague(ApiLeague league);

    Venue toVenue(ApiVenue venue);
}
