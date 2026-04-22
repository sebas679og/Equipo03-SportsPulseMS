package com.sportspulse.teams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Main application class for Auth Service. */
@SpringBootApplication
@EnableFeignClients
@EnableCaching
public class MsTeamsApplication {

  public static void main(String[] args) {
    SpringApplication.run(MsTeamsApplication.class, args);
  }
}
