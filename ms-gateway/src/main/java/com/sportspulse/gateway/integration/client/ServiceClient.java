package com.sportspulse.gateway.integration.client;

import com.sportspulse.gateway.utils.enums.ServiceStatus;
import reactor.core.publisher.Mono;

/**
 * ServiceClient Defines the contract for interacting with external services. Provides a method to
 * retrieve the health status of a service.
 */
public interface ServiceClient {

  /**
   * Retrieves the health status of a service from the given base URL.
   *
   * @param baseUrl the base URL of the service
   * @param serviceName the name of the service for logging and identification purposes
   * @return a ServiceResponse containing the service status
   */
  Mono<ServiceStatus> getHealthService(String serviceName, String baseUrl);
}
