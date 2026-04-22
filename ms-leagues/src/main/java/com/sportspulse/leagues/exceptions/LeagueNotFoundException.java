package com.sportspulse.leagues.exceptions;

/** Exception used when league ID does not exist. */
public class LeagueNotFoundException extends RuntimeException {

  public LeagueNotFoundException() {
    super("No existe una liga con el ID proporcionado");
  }
}
