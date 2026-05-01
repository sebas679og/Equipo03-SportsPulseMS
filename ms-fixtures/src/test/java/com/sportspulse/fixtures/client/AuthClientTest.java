package com.sportspulse.fixtures.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.sportspulse.fixtures.dto.internal.InternalUserResponse;
import com.sportspulse.fixtures.exceptions.BadGatewayException;
import com.sportspulse.fixtures.exceptions.UnauthorizedException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AuthClientTest {

  @Mock private WebClient webClient;

  @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

  @Mock
  @SuppressWarnings("rawtypes")
  private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  private AuthClient authClient;

  @BeforeEach
  void setUp() {
    authClient = new AuthClient(webClient);

    doReturn(requestHeadersUriSpec).when(webClient).get();
    doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString());
    doReturn(requestHeadersSpec).when(requestHeadersSpec).header(anyString(), anyString());
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
  }

  @Test
  void isTokenValid_shouldReturnUser_whenResponseIsSuccessful() {

    UUID id = UUID.randomUUID();
    InternalUserResponse mockUser = new InternalUserResponse(id, "football", "USER", true);

    when(responseSpec.bodyToMono(InternalUserResponse.class)).thenReturn(Mono.just(mockUser));

    InternalUserResponse result = authClient.isTokenValid("valid-token");

    assertNotNull(result);
    assertEquals(id, result.userId());
    assertEquals("football", result.username());
    assertTrue(result.valid());
  }

  @Test
  void isTokenValid_shouldThrowBadGateway_whenBodyIsEmpty() {

    when(responseSpec.bodyToMono(InternalUserResponse.class)).thenReturn(Mono.empty());

    assertThrows(BadGatewayException.class, () -> authClient.isTokenValid("token-without-body"));
  }

  @Test
  void isTokenValid_shouldPropagateException_whenErrorOccurs() {

    when(responseSpec.bodyToMono(InternalUserResponse.class))
        .thenReturn(Mono.error(new UnauthorizedException("Token expired")));

    assertThrows(UnauthorizedException.class, () -> authClient.isTokenValid("expired-token"));
  }
}
