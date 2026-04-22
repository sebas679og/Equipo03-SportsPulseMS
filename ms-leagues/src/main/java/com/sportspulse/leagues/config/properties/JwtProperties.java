package com.sportspulse.leagues.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/** JWT settings*/
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")

public class JwtProperties{

    private String secret;
    private String tokenType;
}