package com.sportspulse.fixtures.mapper;

import com.sportspulse.fixtures.dto.external.FootballFixtureItem;
import com.sportspulse.fixtures.dto.external.FootballFixtureLeague;
import com.sportspulse.fixtures.dto.external.FootballFixtureStatus;
import com.sportspulse.fixtures.dto.external.FootballFixtureVenue;
import com.sportspulse.fixtures.dto.external.event.FootballEventPlayer;
import com.sportspulse.fixtures.dto.external.event.FootballEventTeam;
import com.sportspulse.fixtures.dto.external.event.FootballFixtureEventItem;
import com.sportspulse.fixtures.dto.external.live.FootballLiveItem;
import com.sportspulse.fixtures.dto.external.live.FootballLiveStatus;
import com.sportspulse.fixtures.dto.response.FixtureResponse;
import com.sportspulse.fixtures.dto.response.LeagueFixtureResponse;
import com.sportspulse.fixtures.dto.response.StatusFixtureResponse;
import com.sportspulse.fixtures.dto.response.VenueFixtureResponse;
import com.sportspulse.fixtures.dto.response.event.EventPlayerResponse;
import com.sportspulse.fixtures.dto.response.event.EventTeamResponse;
import com.sportspulse.fixtures.dto.response.event.FixtureEventResponse;
import com.sportspulse.fixtures.dto.response.live.FixtureLiveResponse;
import com.sportspulse.fixtures.dto.response.live.LiveStatusResponse;
import com.sportspulse.fixtures.enums.FixtureLiveStatus;
import com.sportspulse.fixtures.enums.FixtureStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;

/**
 * MapStruct Mapper for converting external API models to SportsPulse internal responses.
 *
 * <p>Handles the complex mapping of nested objects (Fixture, Teams, League) into flattened records.
 * It also manages the conversion between external string-based statuses and internal type-safe
 * Enums using specialized ValueMappings.
 */
@Mapper(componentModel = "spring")
public interface FixtureMapper {

  @Mapping(target = "id", source = "fixture.id")
  @Mapping(target = "date", source = "fixture.date")
  @Mapping(target = "status", source = "fixture.status")
  @Mapping(target = "homeTeam", source = "teams.home")
  @Mapping(target = "awayTeam", source = "teams.away")
  @Mapping(target = "venue", source = "fixture.venue")
  FixtureResponse toFixtureResponse(FootballFixtureItem item);

  @Mapping(target = "elapsed", source = "time.elapsed")
  FixtureEventResponse toFixtureEventResponse(FootballFixtureEventItem item);

  @Mapping(target = "id", source = "fixture.id")
  @Mapping(target = "elapsed", source = "fixture.status.elapsed")
  @Mapping(target = "status", source = "fixture.status")
  @Mapping(target = "homeTeam", source = "teams.home")
  @Mapping(target = "awayTeam", source = "teams.away")
  FixtureLiveResponse toLiveFixtureResponse(FootballLiveItem item);

  @Mapping(target = "status", source = "shortStatus")
  @Mapping(target = "description", source = "longStatus")
  StatusFixtureResponse toStatusFixtureResponse(FootballFixtureStatus status);

  @Mapping(target = "status", source = "shortStatus")
  @Mapping(target = "description", source = "longStatus")
  LiveStatusResponse toLiveStatusResponse(FootballLiveStatus status);

  @ValueMapping(target = "_1H", source = "1H")
  @ValueMapping(target = "_2H", source = "2H")
  @ValueMapping(target = "HT", source = "HT")
  FixtureLiveStatus stringToEnum(String status);

  @ValueMapping(source = "1H", target = "_1H")
  @ValueMapping(source = "2H", target = "_2H")
  @ValueMapping(source = MappingConstants.ANY_REMAINING, target = "UNKNOWN")
  FixtureStatus mapStringToFixtureStatus(String status);

  LeagueFixtureResponse toLeagueFixtureResponse(FootballFixtureLeague league);

  VenueFixtureResponse toVenueFixtureResponse(FootballFixtureVenue venue);

  EventTeamResponse toEventTeamResponse(FootballEventTeam team);

  EventPlayerResponse toEventPlayerResponse(FootballEventPlayer player);
}
