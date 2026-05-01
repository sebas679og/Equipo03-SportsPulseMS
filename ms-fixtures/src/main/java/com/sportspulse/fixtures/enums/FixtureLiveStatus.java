package com.sportspulse.fixtures.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * Enum representing the specific time-periods of an active football match.
 *
 * <p>This enum is specifically tailored for live tracking, identifying whether a match is in the
 * first half, second half, or currently at the break. The {@code @JsonProperty} annotations ensure
 * correct mapping from external API strings that start with numbers, which are invalid as raw Java
 * identifiers.
 */
@Getter
public enum FixtureLiveStatus {
  @JsonProperty("1H")
  _1H("1H"),
  @JsonProperty("2H")
  _2H("2H"),
  HT("HT");

  private final String code;

  FixtureLiveStatus(String code) {
    this.code = code;
  }
}
