package com.sportspulse.auth.utils.security.extractor;

import com.sportspulse.auth.config.constants.InternalHeaders;
import com.sportspulse.auth.config.properties.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** InternalApiKeyExtractor Validates internal API key requests by checking the HTTP header. */
@Component
@RequiredArgsConstructor
public class InternalApiKeyExtractor {

  private final SecurityProperties securityProperties;

  public boolean isValid(HttpServletRequest request) {
    String apiKey = request.getHeader(InternalHeaders.INTERNAL_API_KEY);
    return apiKey != null && apiKey.equals(securityProperties.getInternalApiKey());
  }
}
