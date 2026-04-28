package com.sportspulse.standings.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;
import com.sportspulse.standings.dtos.responses.complements.League;
import com.sportspulse.standings.dtos.responses.complements.Standing;
import com.sportspulse.standings.dtos.responses.complements.Team;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StandingsLeagueAndSeasonResponseTest {

  private League sampleLeague() {
    return League.builder().id(39).name("Premier League").country("England").build();
  }

  private Team sampleTeam(int id, String name) {
    return Team.builder().id(id).name(name).build();
  }

  private Standing sampleStanding(int rank, Team team, int points) {
    return Standing.builder()
        .rank(rank)
        .team(team)
        .points(points)
        .played(30)
        .won(20)
        .drawn(5)
        .lost(5)
        .goalsFor(60)
        .goalsAgainst(25)
        .goalDifference(35)
        .form("WWDWL")
        .build();
  }

  private List<Standing> sampleStandings() {
    return List.of(
        sampleStanding(1, sampleTeam(42, "Arsenal"), 75),
        sampleStanding(2, sampleTeam(50, "Manchester City"), 73));
  }

  // ─── Builder ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("Builder should set all fields correctly")
  void builder_shouldPopulateAllFields() {
    League league = sampleLeague();
    List<Standing> standings = sampleStandings();

    StandingsLeagueAndSeasonResponse response =
        StandingsLeagueAndSeasonResponse.builder().league(league).standings(standings).build();

    assertThat(response.getLeague()).isEqualTo(league);
    assertThat(response.getStandings()).isEqualTo(standings);
  }

  @Test
  @DisplayName("Builder should allow null league")
  void builder_whenLeagueIsNull_shouldBuildSuccessfully() {
    StandingsLeagueAndSeasonResponse response =
        StandingsLeagueAndSeasonResponse.builder()
            .league(null)
            .standings(sampleStandings())
            .build();

    assertThat(response.getLeague()).isNull();
  }

  @Test
  @DisplayName("Builder should allow null standings")
  void builder_whenStandingsIsNull_shouldBuildSuccessfully() {
    StandingsLeagueAndSeasonResponse response =
        StandingsLeagueAndSeasonResponse.builder().league(sampleLeague()).standings(null).build();

    assertThat(response.getStandings()).isNull();
  }

  @Test
  @DisplayName("Builder should allow an empty standings list")
  void builder_whenStandingsIsEmpty_shouldBuildSuccessfully() {
    StandingsLeagueAndSeasonResponse response =
        StandingsLeagueAndSeasonResponse.builder()
            .league(sampleLeague())
            .standings(Collections.emptyList())
            .build();

    assertThat(response.getStandings()).isEmpty();
  }

  @Test
  @DisplayName("Builder should preserve standings list order")
  void builder_shouldPreserveStandingsOrder() {
    List<Standing> standings = sampleStandings();

    StandingsLeagueAndSeasonResponse response =
        StandingsLeagueAndSeasonResponse.builder()
            .league(sampleLeague())
            .standings(standings)
            .build();

    assertThat(response.getStandings()).hasSize(2).containsExactlyElementsOf(standings);
  }

  // ─── Immutability (@Value) ────────────────────────────────────────────────

  @Test
  @DisplayName("Should not expose any setter methods")
  void value_shouldExposeNoSetters() {
    Method[] methods = StandingsLeagueAndSeasonResponse.class.getMethods();

    assertThat(methods).extracting(Method::getName).doesNotContain("setLeague", "setStandings");
  }

  @Test
  @DisplayName("All fields should be declared final")
  void value_allFieldsShouldBeFinal() {
    Field[] fields = StandingsLeagueAndSeasonResponse.class.getDeclaredFields();

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
    League league = sampleLeague();
    List<Standing> standings = sampleStandings();

    StandingsLeagueAndSeasonResponse a =
        StandingsLeagueAndSeasonResponse.builder().league(league).standings(standings).build();

    StandingsLeagueAndSeasonResponse b =
        StandingsLeagueAndSeasonResponse.builder().league(league).standings(standings).build();

    assertThat(a).isEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when league differs")
  void equals_whenLeagueDiffers_shouldReturnFalse() {
    List<Standing> standings = sampleStandings();

    League leagueA = League.builder().id(39).name("Premier League").country("England").build();
    League leagueB = League.builder().id(140).name("La Liga").country("Spain").build();

    StandingsLeagueAndSeasonResponse a =
        StandingsLeagueAndSeasonResponse.builder().league(leagueA).standings(standings).build();

    StandingsLeagueAndSeasonResponse b =
        StandingsLeagueAndSeasonResponse.builder().league(leagueB).standings(standings).build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("equals() should return false when standings differ")
  void equals_whenStandingsDiffer_shouldReturnFalse() {
    League league = sampleLeague();

    List<Standing> standingsA = List.of(sampleStanding(1, sampleTeam(42, "Arsenal"), 75));
    List<Standing> standingsB = List.of(sampleStanding(1, sampleTeam(49, "Chelsea"), 60));

    StandingsLeagueAndSeasonResponse a =
        StandingsLeagueAndSeasonResponse.builder().league(league).standings(standingsA).build();

    StandingsLeagueAndSeasonResponse b =
        StandingsLeagueAndSeasonResponse.builder().league(league).standings(standingsB).build();

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  @DisplayName("hashCode() should be equal for two instances with identical fields")
  void hashCode_whenSameFields_shouldBeEqual() {
    League league = sampleLeague();
    List<Standing> standings = sampleStandings();

    StandingsLeagueAndSeasonResponse a =
        StandingsLeagueAndSeasonResponse.builder().league(league).standings(standings).build();

    StandingsLeagueAndSeasonResponse b =
        StandingsLeagueAndSeasonResponse.builder().league(league).standings(standings).build();

    assertThat(a).hasSameHashCodeAs(b);
  }

  @Test
  @DisplayName("hashCode() should differ when fields differ")
  void hashCode_whenFieldsDiffer_shouldDiffer() {
    StandingsLeagueAndSeasonResponse a =
        StandingsLeagueAndSeasonResponse.builder()
            .league(sampleLeague())
            .standings(sampleStandings())
            .build();

    StandingsLeagueAndSeasonResponse b =
        StandingsLeagueAndSeasonResponse.builder()
            .league(League.builder().id(999).name("Other League").country("Other").build())
            .standings(Collections.emptyList())
            .build();

    assertThat(a.hashCode()).isNotEqualTo(b.hashCode());
  }

  // ─── toString ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("toString() should contain league and standings information")
  void toString_shouldContainFieldValues() {
    StandingsLeagueAndSeasonResponse response =
        StandingsLeagueAndSeasonResponse.builder()
            .league(sampleLeague())
            .standings(sampleStandings())
            .build();

    assertThat(response.toString()).contains("league").contains("standings");
  }

  // ─── Getters ──────────────────────────────────────────────────────────────

  @Test
  @DisplayName("getLeague() should return the league set via builder")
  void getLeague_shouldReturnCorrectValue() {
    League league = sampleLeague();

    StandingsLeagueAndSeasonResponse response =
        StandingsLeagueAndSeasonResponse.builder()
            .league(league)
            .standings(sampleStandings())
            .build();

    assertThat(response.getLeague()).isEqualTo(league);
  }

  @Test
  @DisplayName("getStandings() should return the standings list set via builder")
  void getStandings_shouldReturnCorrectValue() {
    List<Standing> standings = sampleStandings();

    StandingsLeagueAndSeasonResponse response =
        StandingsLeagueAndSeasonResponse.builder()
            .league(sampleLeague())
            .standings(standings)
            .build();

    assertThat(response.getStandings()).hasSize(2).isEqualTo(standings);
  }
}
