package com.sportspulse.standings.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.standings.dtos.responses.TeamStandingLeagueAndSeasonResponse;
import com.sportspulse.standings.dtos.responses.complements.LeagueTeam;
import com.sportspulse.standings.dtos.responses.complements.StandingTeam;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TeamStandingLeagueAndSeasonResponseTest {

  private StandingTeam sampleStandingTeam() {
    return StandingTeam.builder().id(42).name("Arsenal").build();
  }

  private LeagueTeam sampleLeagueTeam() {
    return LeagueTeam.builder().id(39).name("Premier League").build();
  }

  private TeamStandingLeagueAndSeasonResponse sampleResponse() {
    return TeamStandingLeagueAndSeasonResponse.builder()
        .team(sampleStandingTeam())
        .league(sampleLeagueTeam())
        .season(2023)
        .rank(1)
        .points(75)
        .played(30)
        .form("WWDWW")
        .description("Champions League")
        .build();
  }

  // ─── Builder ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Builder should set all fields correctly")
  void builder_shouldPopulateAllFields() {
    StandingTeam team = sampleStandingTeam();
    LeagueTeam league = sampleLeagueTeam();

    TeamStandingLeagueAndSeasonResponse response =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(team)
            .league(league)
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(response.getTeam()).isEqualTo(team);
    assertThat(response.getLeague()).isEqualTo(league);
    assertThat(response.getSeason()).isEqualTo(2023);
    assertThat(response.getRank()).isEqualTo(1);
    assertThat(response.getPoints()).isEqualTo(75);
    assertThat(response.getPlayed()).isEqualTo(30);
    assertThat(response.getForm()).isEqualTo("WWDWW");
    assertThat(response.getDescription()).isEqualTo("Champions League");
  }

  @Test
  @DisplayName("Builder should allow null team")
  void builder_whenTeamIsNull_shouldBuildSuccessfully() {
    TeamStandingLeagueAndSeasonResponse response =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(null)
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(response.getTeam()).isNull();
  }

  @Test
  @DisplayName("Builder should allow null league")
  void builder_whenLeagueIsNull_shouldBuildSuccessfully() {
    TeamStandingLeagueAndSeasonResponse response =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(null)
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(response.getLeague()).isNull();
  }

  @Test
  @DisplayName("Builder should allow null form")
  void builder_whenFormIsNull_shouldBuildSuccessfully() {
    TeamStandingLeagueAndSeasonResponse response =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form(null)
            .description("Champions League")
            .build();

    assertThat(response.getForm()).isNull();
  }

  @Test
  @DisplayName("Builder should allow null description")
  void builder_whenDescriptionIsNull_shouldBuildSuccessfully() {
    TeamStandingLeagueAndSeasonResponse response =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description(null)
            .build();

    assertThat(response.getDescription()).isNull();
  }

  @Test
  @DisplayName("Builder should allow zero values for numeric fields")
  void builder_whenNumericFieldsAreZero_shouldBuildSuccessfully() {
    TeamStandingLeagueAndSeasonResponse response =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(0)
            .rank(0)
            .points(0)
            .played(0)
            .form("LLLLL")
            .description(null)
            .build();

    assertThat(response.getSeason()).isZero();
    assertThat(response.getRank()).isZero();
    assertThat(response.getPoints()).isZero();
    assertThat(response.getPlayed()).isZero();
  }

  // ─── Immutability (@Value) ────────────────────────────────────────────────

  @Test
  @DisplayName("Should not expose any setter methods")
  void value_shouldExposeNoSetters() {
    Method[] methods = TeamStandingLeagueAndSeasonResponse.class.getMethods();

    assertThat(methods)
        .extracting(Method::getName)
        .doesNotContain(
            "setTeam",
            "setLeague",
            "setSeason",
            "setRank",
            "setPoints",
            "setPlayed",
            "setForm",
            "setDescription");
  }

  @Test
  @DisplayName("All fields should be declared final")
  void value_allFieldsShouldBeFinal() {
    Field[] fields = TeamStandingLeagueAndSeasonResponse.class.getDeclaredFields();

    assertThat(fields)
        .allSatisfy(
            field ->
                assertThat(Modifier.isFinal(field.getModifiers()))
                    .as("Field '%s' should be final", field.getName())
                    .isTrue());
  }

  // ─── equals & hashCode ────────────────────────────────────────────────────

  @Test
  @DisplayName("equals() should return true for two instances with identical fields")
  void equals_whenSameFields_shouldReturnTrue() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();
    TeamStandingLeagueAndSeasonResponse b = sampleResponse();

    assertThat(a).isEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when team differs")
  void equals_whenTeamDiffers_shouldReturnFalse() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(StandingTeam.builder().id(50).name("Manchester City").build())
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when league differs")
  void equals_whenLeagueDiffers_shouldReturnFalse() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(LeagueTeam.builder().id(140).name("La Liga").build())
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when season differs")
  void equals_whenSeasonDiffers_shouldReturnFalse() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(2022)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when rank differs")
  void equals_whenRankDiffers_shouldReturnFalse() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(2)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when points differs")
  void equals_whenPointsDiffers_shouldReturnFalse() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(1)
            .points(60)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when played differs")
  void equals_whenPlayedDiffers_shouldReturnFalse() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(1)
            .points(75)
            .played(28)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when form differs")
  void equals_whenFormDiffers_shouldReturnFalse() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("LLDWW")
            .description("Champions League")
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when description differs")
  void equals_whenDescriptionDiffers_shouldReturnFalse() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(sampleStandingTeam())
            .league(sampleLeagueTeam())
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Europa League")
            .build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("hashCode() should be equal for two instances with identical fields")
  void hashCode_whenSameFields_shouldBeEqual() {
    assertThat(sampleResponse()).hasSameHashCodeAs(sampleResponse());
  }

  @Test
  @DisplayName("hashCode() should differ when fields differ")
  void hashCode_whenFieldsDiffer_shouldDiffer() {
    TeamStandingLeagueAndSeasonResponse a = sampleResponse();

    TeamStandingLeagueAndSeasonResponse b =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(StandingTeam.builder().id(999).name("Other Team").build())
            .league(LeagueTeam.builder().id(999).name("Other League").build())
            .season(2000)
            .rank(20)
            .points(10)
            .played(30)
            .form("LLLLL")
            .description(null)
            .build();

    assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
  }

  // ─── toString ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("toString() should contain all field values")
  void toString_shouldContainAllFieldValues() {
    TeamStandingLeagueAndSeasonResponse response = sampleResponse();

    assertThat(response.toString())
        .contains("team")
        .contains("league")
        .contains("2023")
        .contains("1")
        .contains("75")
        .contains("30")
        .contains("WWDWW")
        .contains("Champions League");
  }

  // ─── Getters ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Getters should return the values set via builder")
  void getters_shouldReturnCorrectValues() {
    StandingTeam team = sampleStandingTeam();
    LeagueTeam league = sampleLeagueTeam();

    TeamStandingLeagueAndSeasonResponse response =
        TeamStandingLeagueAndSeasonResponse.builder()
            .team(team)
            .league(league)
            .season(2023)
            .rank(1)
            .points(75)
            .played(30)
            .form("WWDWW")
            .description("Champions League")
            .build();

    assertThat(response.getTeam()).isEqualTo(team);
    assertThat(response.getLeague()).isEqualTo(league);
    assertThat(response.getSeason()).isEqualTo(2023);
    assertThat(response.getRank()).isEqualTo(1);
    assertThat(response.getPoints()).isEqualTo(75);
    assertThat(response.getPlayed()).isEqualTo(30);
    assertThat(response.getForm()).isEqualTo("WWDWW");
    assertThat(response.getDescription()).isEqualTo("Champions League");
  }
}
