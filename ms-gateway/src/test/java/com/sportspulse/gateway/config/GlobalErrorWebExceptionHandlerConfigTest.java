package com.sportspulse.gateway.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.gateway.exceptions.GlobalErrorWebExceptionHandler;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.codec.ServerCodecConfigurer;

@DisplayName("GlobalErrorWebExceptionHandlerConfig Tests")
class GlobalErrorWebExceptionHandlerConfigTest {

  @Test
  @DisplayName("bean is created and is not null")
  void globalErrorWebExceptionHandler_beanIsNotNull() {
    ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();

    GlobalErrorWebExceptionHandler handler =
        new GlobalErrorWebExceptionHandlerConfig()
            .globalErrorWebExceptionHandler(
                mock(ErrorAttributes.class),
                new WebProperties(),
                new AnnotationConfigApplicationContext(),
                mock(ObjectMapper.class),
                codecConfigurer,
                Collections.emptyList());

    assertThat(handler).isNotNull();
  }
}
