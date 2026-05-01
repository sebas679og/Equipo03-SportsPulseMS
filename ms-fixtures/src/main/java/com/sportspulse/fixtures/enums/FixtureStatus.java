package com.sportspulse.fixtures.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enum representing the comprehensive set of possible match states.
 *
 * <p>This enum covers the entire lifecycle of a fixture, from scheduling (NS, TBD) through live
 * play (1H, HT, ET) to final results (FT, PEN) and cancellations (PST, ABD). It aligns with
 * standard sports data provider codes to ensure consistent status tracking.
 */
public enum FixtureStatus {
  NS,
  TBD,
  @JsonProperty("1H")
  _1H,
  @JsonProperty("2H")
  _2H,
  HT,
  ET,
  BT,
  P,
  LIVE,
  SUSP,
  INT,
  FT,
  AET,
  PEN,
  PST,
  CANC,
  ABD,
  AWD,
  WO,
  UNKNOWN
}
