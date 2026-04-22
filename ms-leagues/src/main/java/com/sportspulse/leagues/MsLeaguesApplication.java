package com.sportspulse.leagues;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Main application class for Leagues Service. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class MsLeaguesApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsLeaguesApplication.class, args);
  }
}
