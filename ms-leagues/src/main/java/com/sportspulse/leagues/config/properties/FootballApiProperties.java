package com.sportspulse.leagues.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/** External API-Football configuration. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sports-pulse.api")
public class FootballApiProperties{
    private String baseUrl;
    private String key;

}