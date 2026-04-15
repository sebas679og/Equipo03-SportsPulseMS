package com.sportspulse.gateway.integration.client;

import com.sportspulse.gateway.integration.dtos.responses.ServiceResponse;

/**
 * ServiceClient Defines the contract for interacting with external services. Provides a method to
 * retrieve the health status of a service.
 */
public interface ServiceClient {

  /**
   * Retrieves the health status of a service from the given base URL.
   *
   * @param baseUrl the base URL of the service
   * @return a ServiceResponse containing the service status
   */
  ServiceResponse getHealthService(String baseUrl);
}
