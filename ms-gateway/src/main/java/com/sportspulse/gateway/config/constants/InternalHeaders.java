package com.sportspulse.gateway.config.constants;

/** InternalHeaders Defines constant values for internal HTTP headers. */
public final class InternalHeaders {

  private InternalHeaders() {}

  public static final String CLIENT_IP_HEADER = "X-Forwarded-For";
  public static final String AUTHORIZATION = "Authorization";
  public static final String CONTENT_TYPE = "Content-Type";
}
