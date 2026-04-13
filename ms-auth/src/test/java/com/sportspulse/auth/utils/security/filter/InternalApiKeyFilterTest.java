package com.sportspulse.auth.utils.security.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.auth.config.constants.ApiPaths;
import com.sportspulse.auth.config.constants.InternalHeaders;
import com.sportspulse.auth.utils.security.extractor.InternalApiKeyExtractor;
import com.sportspulse.auth.utils.security.writer.HttpErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalApiKeyFilter")
class InternalApiKeyFilterTest {

  @Mock private InternalApiKeyExtractor apiKeyExtractor;
  @Mock private HttpErrorResponseWriter errorResponseWriter;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  @InjectMocks private InternalApiKeyFilter filter;

  // ---------------------------------------------------------------------------
  // doFilterInternal
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("doFilterInternal() proceeds to filter chain when API key is valid")
  void doFilterInternal_proceedsChain_whenApiKeyIsValid() throws ServletException, IOException {
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY)).thenReturn("valid-key");
    when(apiKeyExtractor.isValid(request)).thenReturn(true);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verify(errorResponseWriter, never()).write(any(), any(), any());
  }

  @Test
  @DisplayName("doFilterInternal() writes 401 and halts chain when API key header is missing")
  void doFilterInternal_writes401_whenApiKeyHeaderIsMissing() throws ServletException, IOException {
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY)).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(errorResponseWriter)
        .write(response, HttpStatus.UNAUTHORIZED, "Missing internal API key");
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() writes 403 and halts chain when API key is invalid")
  void doFilterInternal_writes403_whenApiKeyIsInvalid() throws ServletException, IOException {
    when(request.getHeader(InternalHeaders.INTERNAL_API_KEY)).thenReturn("wrong-key");
    when(apiKeyExtractor.isValid(request)).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    verify(errorResponseWriter).write(response, HttpStatus.FORBIDDEN, "Invalid internal API key");
    verify(filterChain, never()).doFilter(request, response);
  }

  // ---------------------------------------------------------------------------
  // shouldNotFilter
  // ---------------------------------------------------------------------------

  @Test
  @DisplayName("shouldNotFilter() returns false for the validate-token path (filter applies)")
  void shouldNotFilter_returnsFalse_whenUriIsValidateTokenPath() {
    when(request.getRequestURI()).thenReturn(ApiPaths.Validate.TOKEN);

    boolean result = filter.shouldNotFilter(request);

    org.assertj.core.api.Assertions.assertThat(result).isFalse();
  }

  @Test
  @DisplayName("shouldNotFilter() returns true for any other path (filter is skipped)")
  void shouldNotFilter_returnsTrue_whenUriIsNotValidateTokenPath() {
    when(request.getRequestURI()).thenReturn("/api/v1/some/other/endpoint");

    boolean result = filter.shouldNotFilter(request);

    org.assertj.core.api.Assertions.assertThat(result).isTrue();
  }

  @Test
  @DisplayName("shouldNotFilter() returns true for the root path")
  void shouldNotFilter_returnsTrue_whenUriIsRoot() {
    when(request.getRequestURI()).thenReturn("/");

    boolean result = filter.shouldNotFilter(request);

    org.assertj.core.api.Assertions.assertThat(result).isTrue();
  }
}
