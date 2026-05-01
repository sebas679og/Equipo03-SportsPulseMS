package com.sportspulse.fixtures.enums;

/**
 * Enum used to filter fixture queries based on their current lifecycle stage.
 *
 * <p>This simplified set of statuses is typically used in Request Parameters to allow clients to
 * toggle between upcoming matches, active games, or completed results without needing to know the
 * granular internal codes (like 'ABD' or 'AET').
 */
public enum FixtureStatusRequest {
  NS,
  LIVE,
  FT
}
