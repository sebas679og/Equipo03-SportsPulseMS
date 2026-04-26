package com.sportspulse.leagues.integration.msauth;

import com.sportspulse.leagues.config.constants.InternalHeaders;
import com.sportspulse.leagues.config.properties.MsAuthProperties;
import com.sportspulse.leagues.exceptions.CustomServiceUnavailableException;
import com.sportspulse.leagues.exceptions.CustomUnauthorizedException;
import com.sportspulse.leagues.integration.msauth.dto.UserResponse;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class AuthClientImpl implements AuthClient {

  private final RestTemplate restTemplate;
  private final MsAuthProperties msAuthProperties;

  @Override
  public UserResponse isTokenValid(String token) {
    URI uri =
        UriComponentsBuilder.fromUriString(msAuthProperties.getBaseUrl())
            .path("/api/auth/validate")
            .build(true)
            .toUri();

    HttpHeaders headers = new HttpHeaders();
    headers.set(
        InternalHeaders.MsAuth.BEARER_HEADER,
        String.join(" ", InternalHeaders.MsAuth.TYPE_TOKEN, token));
    headers.set(InternalHeaders.MsAuth.MS_AUTH_KEY, msAuthProperties.getApiKey());

    try {
      ResponseEntity<UserResponse> response =
          restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), UserResponse.class);

      UserResponse body = response.getBody();
      if (body == null || !body.valid()) {
        throw new CustomUnauthorizedException("Token inválido");
      }
      return body;
    } catch (RestClientException ex) {
      if (ex instanceof RestClientResponseException responseException
          && responseException.getStatusCode().is4xxClientError()) {
        throw new CustomUnauthorizedException("Token inválido");
      }
      throw new CustomServiceUnavailableException("No se pudo validar el token con ms-auth");
    }
  }
}