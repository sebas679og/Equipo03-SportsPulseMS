package com.sportspulse.auth.utils.security.extractor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.sportspulse.auth.config.constants.InternalHeaders;
import com.sportspulse.auth.config.properties.SecurityProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalApiKeyExtractor")
class InternalApiKeyExtractorTest {

  private static final String VALID_API_KEY = "secret-internal-key";

  @Mock private SecurityProperties securityProperties;

  @Mock private HttpServletRequest request;

  @InjectMocks private InternalApiKeyExtractor extractor;

  @Test
  @DisplayName("isValid() returns true when header matches the configured API key")
  void isValid_returnsTrue_whenApiKeyMatches() {
    when(securityProperties.getInternalApiKey()).thenReturn(VALID_API_KEY);
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY)).thenReturn(VALID_API_KEY);

    assertThat(extractor.isValid(request)).isTrue();
  }

  @Test
  @DisplayName("isValid() returns false when header value does not match the configured API key")
  void isValid_returnsFalse_whenApiKeyDoesNotMatch() {
    when(securityProperties.getInternalApiKey()).thenReturn(VALID_API_KEY);
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY)).thenReturn("wrong-key");

    assertThat(extractor.isValid(request)).isFalse();
  }

  @Test
  @DisplayName("isValid() returns false when the API key header is missing (null)")
  void isValid_returnsFalse_whenApiKeyHeaderIsNull() {
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY)).thenReturn(null);

    assertThat(extractor.isValid(request)).isFalse();
  }

  @Test
  @DisplayName("isValid() returns false when the API key header is an empty string")
  void isValid_returnsFalse_whenApiKeyHeaderIsEmpty() {
    when(securityProperties.getInternalApiKey()).thenReturn(VALID_API_KEY);
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY)).thenReturn("");

    assertThat(extractor.isValid(request)).isFalse();
  }

  @Test
  @DisplayName("isValid() returns false when the API key header has leading/trailing whitespace")
  void isValid_returnsFalse_whenApiKeyHasExtraWhitespace() {
    when(securityProperties.getInternalApiKey()).thenReturn(VALID_API_KEY);
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY)).thenReturn(" " + VALID_API_KEY + " ");

    assertThat(extractor.isValid(request)).isFalse();
  }

  @Test
  @DisplayName("isValid() is case-sensitive and returns false for wrong casing")
  void isValid_returnsFalse_whenApiKeyHasWrongCasing() {
    when(securityProperties.getInternalApiKey()).thenReturn(VALID_API_KEY);
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY))
        .thenReturn(VALID_API_KEY.toUpperCase());

    assertThat(extractor.isValid(request)).isFalse();
  }
}
