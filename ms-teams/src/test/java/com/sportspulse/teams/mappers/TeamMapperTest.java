package com.sportspulse.teams.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.teams.dto.external.TeamData;
import com.sportspulse.teams.dto.external.TeamVenueWrapper;
import com.sportspulse.teams.dto.external.VenueData;
import com.sportspulse.teams.dto.response.TeamResponse;
import com.sportspulse.teams.dto.response.VenueResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TeamMapperTest {
  private final TeamMapper teamMapper = Mappers.getMapper(TeamMapper.class);

  private TeamVenueWrapper buildWrapper() {
    TeamData team = new TeamData(33, "Manchester United", "England", "https://logo.png", 1878);
    VenueData venue = new VenueData("Old Trafford", "Manchester", 76212);
    return new TeamVenueWrapper(team, venue);
  }

  @Test
  void teamResponse_shouldMapAllTeamFieldsCorrectly() {

    TeamVenueWrapper wrapper = buildWrapper();

    TeamResponse result = teamMapper.toTeamResponse(wrapper);

    assertThat(result.id()).isEqualTo(33);
    assertThat(result.name()).isEqualTo("Manchester United");
    assertThat(result.country()).isEqualTo("England");
    assertThat(result.logo()).isEqualTo("https://logo.png");
    assertThat(result.founded()).isEqualTo(1878);
  }

  @Test
  void teamResponse_shouldMapVenueCorrectly() {

    TeamVenueWrapper wrapper = buildWrapper();

    TeamResponse result = teamMapper.toTeamResponse(wrapper);

    assertThat(result.venue().name()).isEqualTo("Old Trafford");
    assertThat(result.venue().city()).isEqualTo("Manchester");
    assertThat(result.venue().capacity()).isEqualTo(76212);
  }

  @Test
  void venue_shouldMapAllFieldsCorrectly() {

    VenueData venueData = new VenueData("Old Trafford", "Manchester", 76212);

    VenueResponse result = teamMapper.toVenue(venueData);

    assertThat(result.name()).isEqualTo("Old Trafford");
    assertThat(result.city()).isEqualTo("Manchester");
    assertThat(result.capacity()).isEqualTo(76212);
  }

  @Test
  void teamResponse_withNullVenueFields_shouldMapNulls() {

    TeamData team = new TeamData(1, "Team", "Country", null, null);
    VenueData venue = new VenueData(null, null, null);
    TeamVenueWrapper wrapper = new TeamVenueWrapper(team, venue);

    TeamResponse result = teamMapper.toTeamResponse(wrapper);

    assertThat(result.logo()).isNull();
    assertThat(result.founded()).isNull();
    assertThat(result.venue().name()).isNull();
    assertThat(result.venue().city()).isNull();
    assertThat(result.venue().capacity()).isNull();
  }
}
