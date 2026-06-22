package com.sportspulse.fixtures.config;

import com.sportspulse.fixtures.utils.Status;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(
                String.class,
                Status.class,
                source -> {
                    try {
                        return Status.valueOf(source.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(
                                "Invalid transport type: '%s'. Valid values: %s"
                                        .formatted(source, Arrays.toString(Status.values())));
                    }
                });
    }
}
